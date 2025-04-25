/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore
import kotlin.Exception

class Xp4MappingFragment : DecoderFragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Xp4MappingViewModel>()
    private lateinit var rvAdapter: Xp4MappingRecyclerViewAdapter

    private lateinit var emptyView: TextView
    private lateinit var listView: RecyclerView

    companion object {
        fun newInstance() = Xp4MappingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_xp4_mappng, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            emptyView = findViewById(R.id.textEmpty)
            listView = findViewById(R.id.rvMapping)
        }

        emptyView.setOnClickListener {
            showReloadDialog()
        }

        rvAdapter = Xp4MappingRecyclerViewAdapter(model)
        with(listView) {
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }

        model.loaded.observe(viewLifecycleOwner) {
            if (it) {
                emptyView.visibility = View.GONE
                listView.visibility = View.VISIBLE
            }
            else {
                emptyView.visibility = View.VISIBLE
                listView.visibility = View.GONE
            }
        }

        model.editRowIndex.observe(viewLifecycleOwner) {
            if (it != null) AlertDialog.Builder(context)
                .setTitle(getString(
                    R.string.title_dialog_xp4_row_x,
                    it + 1
                ))
                .setView(createEditRowDialog())
                .setPositiveButton(R.string.action_write) { _, _ -> writeEditRowCVs() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> model.editRow(null) }
                .show()
        }

        model.reloadRowIndex.observe(viewLifecycleOwner) {
            if (it != null) AlertDialog.Builder(context)
                .setTitle(getString(
                    R.string.title_dialog_xp4_row_x,
                    it + 1
                ))
                .setMessage(R.string.message_reload_cvs)
                .setPositiveButton(R.string.action_reload) { _, _ -> readRowCVs() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> model.reloadRow(null) }
                .show()
        }

        if ((savedInstanceState == null) && (model.loaded.value == true)) {
            Snackbar.make(view, R.string.message_cvs_outdated, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_reload) {
                    showReloadDialog()
                }.show()
        }
    }

    private fun showReloadDialog() = AlertDialog.Builder(context)
        .setTitle(R.string.title_dialog_read_method)
        .setMessage(R.string.message_xp4_read_strategy)
        .setPositiveButton(R.string.action_read_all) { _, _ -> readAllCVs() }
        .setNegativeButton(R.string.action_read_until_blank) { _, _ -> readAllCVs(true) }
        .setNeutralButton(android.R.string.cancel, null)
        .show()

    private fun readAllCVs(untilBlank: Boolean = false) {
        val maxCvs = Xp4MappingViewModel.ROWS * Xp4MappingViewModel.COLS
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            if (untilBlank) {
                setMax(0)
                setTitle(R.string.title_dialog_reading_cvs)
            }
            else {
                setMax(maxCvs)
                setTitle(getString(R.string.title_dialog_reading_x_cvs, maxCvs))
            }
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            show()
        }

        model.setLoaded(false)

        job = lifecycleScope.launch {
            try {
                checkManufacturer()
                writeCv(Xp4MappingViewModel.INDEX_CV1, Xp4MappingViewModel.INDEX_CV1_VALUE)
                var idx = -1
                for (ri in model.rowIndexes) {
                    if (untilBlank) dialog.setMessage(getString(R.string.label_xp4_row_x, (ri + 1)))

                    for (ci in model.colIndexes) {
                        val cvi = model.cvNumber(ri, ci)
                        if (cvi.first != idx) {
                            idx = cvi.first
                            writeCv(Xp4MappingViewModel.INDEX_CV2, idx)
                        }
                        val value = readCv(cvi.second, MockStore::randomXp4MappingCvValue)
                        if (BuildConfig.DEBUG) Log.d(TAG,
                            "Row $ri, col $ci, idx ${cvi.first}, CV ${cvi.second} = $value"
                        )
                        model.setCvValue(ri, ci, value)
                        if (!untilBlank) dialog.incrementProgress()
                    }

                    rvAdapter.notifyItemChanged(ri)
                    if (untilBlank && model.isBlank(ri)) break
                }
                model.setLoaded(true)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    if (BuildConfig.DEBUG) Log.w(TAG, e)
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
                if (BuildConfig.DEBUG && untilBlank) model.setLoaded(true)
            }
            dialog.dismiss()
        }
    }

    private fun createEditRowDialog(): View {
        val context = context!!
        val p = resources.getDimension(R.dimen.dialog_padding)

        val inputsView = ScrollView(context)
        val inputsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(p.toInt())
        }
        inputsView.addView(inputsLayout)
        createEditRowDialogCheckboxes(inputsLayout, model.inputColumnIndexes, true)

        val outputsView = ScrollView(context).apply {
            visibility = View.GONE
        }
        val outputsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(p.toInt())
        }
        outputsView.addView(outputsLayout)
        createEditRowDialogCheckboxes(outputsLayout, model.outputColumnIndexes)

        val tabs = TabLayout(context).apply {
            addTab(newTab().apply {
                text = getString(R.string.label_inputs)
            })
            addTab(newTab().apply {
                text = getString(R.string.label_outputs)
            })

            addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: Tab?) {
                    if (inputsView.isVisible) {
                        inputsView.visibility = View.GONE
                        outputsView.visibility = View.VISIBLE
                    }
                    else {
                        inputsView.visibility = View.VISIBLE
                        outputsView.visibility = View.GONE
                    }
                }

                override fun onTabUnselected(tab: Tab?) {
                    // do nothing
                }

                override fun onTabReselected(tab: Tab?) {
                    // do nothing
                }
            })
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(tabs)
            addView(inputsView)
            addView(outputsView)
        }
    }

    private fun createEditRowDialogCheckboxes(layout: LinearLayout, range: IntRange, reorder: Boolean = false) {
        val context = context!!
        val m = resources.getDimension(R.dimen.text_margin)
        val stringArray = resources.getStringArray(R.array.xp4_mapping_bits)

        val views = mutableListOf<SwitchCompat>()

        for (ci in range) {
            val cvValue = model.getEditRowValue(ci)
            val bi = (ci * 8 until ci * 8 + 8)
            stringArray.slice(bi).forEachIndexed { idx, str ->
                val switchView = SwitchCompat(context).apply {
                    text = str
                    if (cvValue > 0) {
                        val ii = 1 shl idx
                        isChecked = (cvValue and ii == ii)
                    }
                    setPadding(m.toInt(), m.toInt(), 0, m.toInt())
                    setOnCheckedChangeListener { _, chk ->
                        val oldValue = model.getEditRowValue(ci)
                        val newValue =
                            if (chk) oldValue or (1 shl idx)
                            else oldValue and (1 shl idx).inv()
                        model.setEditRowValue(ci, newValue)
                    }
                }
                if (reorder) views.add(switchView)
                else layout.addView(switchView)
            }
        }
        if (reorder) {
            views.indices.forEach {
                val idx =
                    if (it % 2 == 0) it / 2
                    else views.size / 2 + it / 2
                layout.addView(views[idx])
            }
        }
    }

    private fun writeEditRowCVs() {
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setTitle(R.string.title_dialog_writing_cvs)
            setMax(Xp4MappingViewModel.COLS)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            setOnDismissListener { model.editRow(null) }
            show()
        }

        job = lifecycleScope.launch {
            try {
                var idx = 0
                val rowIndex = model.editRowIndex.value!!
                for (ci in model.inputColumnIndexes + model.outputColumnIndexes) {
                    val cvi = model.cvNumber(rowIndex, ci)
                    if (cvi.first != idx) {
                        idx = cvi.first
                        writeCv(Xp4MappingViewModel.INDEX_CV2, idx)
                    }
                    val value = model.getEditRowValue(ci)
                    writeCv(cvi.second, value)
                    model.setCvValue(rowIndex, ci, value)
                    dialog.incrementProgress()
                }
                rvAdapter.notifyItemChanged(rowIndex)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
                model.setLoaded(false)
            }
            dialog.dismiss()
        }
    }

    private fun readRowCVs() {
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setTitle(R.string.title_dialog_reading_cvs)
            setMax(Xp4MappingViewModel.COLS)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            setOnDismissListener { model.reloadRow(null) }
            show()
        }

        job = lifecycleScope.launch {
            try {
                checkManufacturer()
                writeCv(Xp4MappingViewModel.INDEX_CV1, Xp4MappingViewModel.INDEX_CV1_VALUE)

                var idx = -1
                val rowIndex = model.reloadRowIndex.value!!
                for (ci in model.inputColumnIndexes + model.outputColumnIndexes) {
                    val cvi = model.cvNumber(rowIndex, ci)
                    if (cvi.first != idx) {
                        idx = cvi.first
                        writeCv(Xp4MappingViewModel.INDEX_CV2, idx)
                    }
                    val value = readCv(cvi.second, MockStore::randomXp4MappingCvValue)
                    if (BuildConfig.DEBUG) Log.d(TAG,
                        "Row $rowIndex, col $ci, idx ${cvi.first}, CV ${cvi.second} = $value"
                    )
                    model.setCvValue(rowIndex, ci, value)
                    dialog.incrementProgress()
                }
                rvAdapter.notifyItemChanged(rowIndex)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
            }
            dialog.dismiss()
        }
    }
}