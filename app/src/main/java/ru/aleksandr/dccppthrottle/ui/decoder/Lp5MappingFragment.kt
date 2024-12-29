/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.fragment.app.Fragment
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
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore
import kotlin.Exception
import kotlin.coroutines.resume
import kotlin.math.pow

class Lp5MappingFragment : Fragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Lp5MappingViewModel>()
    private lateinit var rvAdapter: Lp5MappingRecyclerViewAdapter

    private lateinit var emptyView: TextView
    private lateinit var listView: RecyclerView

    companion object {
        fun newInstance() = Lp5MappingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lp5_mappng, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            emptyView = findViewById(R.id.empty_view)
            listView = findViewById(R.id.rv_lp5_mapping)
        }

        emptyView.setOnClickListener {
            showReloadDialog()
        }

        rvAdapter = Lp5MappingRecyclerViewAdapter(model)
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
                    R.string.title_dialog_lp5_row_x,
                    it + 1
                ))
                .setView(createEditRowDialog())
                .setPositiveButton(R.string.action_write) { _, _ -> writeEditRowCVs() }
                .setNegativeButton(android.R.string.cancel, null)
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
        .setTitle(R.string.title_dialog_read_strategy)
        .setMessage(R.string.message_lp5_read_strategy)
        .setPositiveButton(R.string.action_read_all) { _, _ -> readAllCVs() }
        .setNegativeButton(R.string.action_read_until_blank) { _, _ -> readUntilBlank() }
        .setNeutralButton(android.R.string.cancel, null)
        .show()

    private fun readAllCVs() {
        val maxCvs = Lp5MappingViewModel.ROWS * Lp5MappingViewModel.COLS
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setMax(maxCvs)
            setTitle(getString(R.string.title_dialog_reading_x_cvs, maxCvs))
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            show()
        }

        model.setLoaded(false)

        job = lifecycleScope.launch {
            try {
                checkManufacturer()

                var idx = 0

                // read conditions
                for (ri in model.rowIndexes) {
                    for (ci in model.inputColumnIndexes) {
                        val cvi = model.cvNumber(ri, ci)
                        if (cvi.first != idx) {
                            idx = cvi.first
                            writeCv(Lp5MappingViewModel.INDEX_CV, idx)
                        }
                        val value = readCv(cvi.second)
                        if (BuildConfig.DEBUG) Log.d(TAG,
                            "Row $ri, col $ci, idx ${cvi.first}, CV ${cvi.second} = $value"
                        )
                        model.setCvValue(ri, ci, value)
                        dialog.incrementProgress()
                    }
                }

                // read outputs
                for (ri in model.rowIndexes) {
                    for (ci in model.outputColumnIndexes) {
                        val cvi = model.cvNumber(ri, ci)
                        if (cvi.first != idx) {
                            idx = cvi.first
                            writeCv(Lp5MappingViewModel.INDEX_CV, idx)
                        }
                        val value = readCv(cvi.second)
                        if (BuildConfig.DEBUG) Log.d(TAG,
                            "Row $ri, col $ci, idx ${cvi.first}, CV ${cvi.second} = $value"
                        )
                        model.setCvValue(ri, ci, value)
                        dialog.incrementProgress()
                    }
                }

                model.setLoaded(true)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                job?.cancel()
            }
            dialog.dismiss()
        }
    }

    private fun readUntilBlank() {
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setMax(0)
            setTitle(R.string.title_dialog_reading_cvs)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            show()
        }

        model.setLoaded(false)

        job = lifecycleScope.launch {
            try {
                checkManufacturer()

                var idx = 0
                for (ri in model.rowIndexes) {
                    dialog.setMessage(getString(R.string.label_lp5_row_x, (ri + 1)))

                    // read conditions
                    for (ci in model.inputColumnIndexes) {
                        val cvi = model.cvNumber(ri, ci)
                        if (cvi.first != idx) {
                            idx = cvi.first
                            writeCv(Lp5MappingViewModel.INDEX_CV, idx)
                        }
                        val value = readCv(cvi.second)
                        if (BuildConfig.DEBUG) Log.d(TAG,
                            "Row $ri, col $ci, idx ${cvi.first}, CV ${cvi.second} = $value"
                        )
                        model.setCvValue(ri, ci, value)
                    }

                    // read outputs
                    for (ci in model.outputColumnIndexes) {
                        val cvi = model.cvNumber(ri, ci)
                        if (cvi.first != idx) {
                            idx = cvi.first
                            writeCv(Lp5MappingViewModel.INDEX_CV, idx)
                        }
                        val value = readCv(cvi.second)
                        if (BuildConfig.DEBUG) Log.d(TAG,
                            "Row $ri, col $ci, idx ${cvi.first}, CV ${cvi.second} = $value"
                        )
                        model.setCvValue(ri, ci, value)
                    }

                    if (model.isBlank(ri)) break
                }
                model.setLoaded(true)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                job?.cancel()
                if (BuildConfig.DEBUG) model.setLoaded(true)
            }
            dialog.dismiss()
        }
    }

    private suspend fun writeCv(cv: Int, value: Int): Boolean = suspendCancellableCoroutine { cont ->
        if (BuildConfig.DEBUG && !CommandStation.isConnected()) {
            Log.d(TAG, "Fake write CV $cv = $value")
            Handler(Looper.getMainLooper()).postDelayed({
                cont.resume(true)
            }, 100)
        }
        else CommandStation.setCvProg(cv, value) { _, value ->
            if (value < 0) throw Exception(getString(R.string.message_write_cv_error, cv))
            cont.resume(value >= 0)
        }
    }


    private suspend fun readCv(cv: Int): Int = suspendCancellableCoroutine { cont ->
        if (BuildConfig.DEBUG && !CommandStation.isConnected()) {
            val value = MockStore.randomLp5ControlCvValue()
            Log.d(TAG, "Fake read CV $cv = $value")
            Handler(Looper.getMainLooper()).postDelayed({
                cont.resume(value)
            }, 100)
        }
        else CommandStation.getCvProg(cv) { _, value ->
            if (value < 0) throw Exception(getString(R.string.message_read_cv_error, cv))
            cont.resume(value)
        }
    }

    private suspend fun checkManufacturer() {
        val cv8 = readCv(Lp5MappingViewModel.MANUFACTURER_CV)
        if (cv8 != Lp5MappingViewModel.MANUFACTURER_ID_ESU) {
            val message = getString(R.string.message_wrong_manufacturer, cv8)
            if (BuildConfig.DEBUG) Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            else throw Exception(message)
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
        createEditRowDialogCheckboxes(inputsLayout, model.inputColumnIndexes)

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
                text = getString(R.string.label_lp5_inputs)
            })
            addTab(newTab().apply {
                text = getString(R.string.label_lp5_outputs)
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

    private fun createEditRowDialogCheckboxes(layout: LinearLayout, rage: IntRange) {
        val context = context!!
        val m = resources.getDimension(R.dimen.text_margin)
        for (ci in rage) {
            val cvValue = model.getEditRowValue(ci)

            val textView = TextView(context).apply {
                val letter = Lp5MappingViewModel.CONTROL_CV_LETTERS[ci]
                text = context.getString(R.string.label_lp5_control_cv_x, letter)
            }
            layout.addView(textView)

            val stringArrayId = Lp5MappingViewModel.CONTROL_CV_STRING_ID[ci]
            resources.getStringArray(stringArrayId).forEachIndexed { idx, str ->
                val switchView = SwitchCompat(context).apply {
                    text = str
                    if (cvValue > 0) {
                        isChecked = (cvValue and 2f.pow(idx).toInt() == cvValue)
                    }
                    setPadding(m.toInt(), m.toInt(), 0, m.toInt())
                    setOnCheckedChangeListener { _, chk ->
                        val oldValue = model.getEditRowValue(ci)
                        val newValue =
                            if (chk) oldValue or 2f.pow(idx).toInt()
                            else oldValue and 2f.pow(idx).toInt().inv()
                        model.setEditRowValue(ci, newValue)
                    }
                }
                layout.addView(switchView)
            }
        }
    }

    private fun writeEditRowCVs() {
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setTitle(R.string.title_dialog_writing_cvs)
            setMax(Lp5MappingViewModel.COLS)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
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
                        writeCv(Lp5MappingViewModel.INDEX_CV, idx)
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
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                job?.cancel()
                model.setLoaded(false)
            }
            dialog.dismiss()
        }
    }
}