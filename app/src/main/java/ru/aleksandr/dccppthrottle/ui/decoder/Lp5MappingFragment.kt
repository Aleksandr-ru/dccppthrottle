/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
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
            AlertDialog.Builder(context)
                .setTitle(R.string.title_dialog_read_strategy)
                .setMessage(R.string.message_lp5_read_strategy)
                .setPositiveButton(R.string.action_read_all) { _, _ ->
                    readAllCVs()
                }
                .setNegativeButton(R.string.action_read_until_blank) { _, _ ->
                    readUntilBlank()
                }
                .setNeutralButton(android.R.string.cancel, null)
                .show()
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
                .setPositiveButton(R.string.label_write) { _, _ ->
                    writeEditRowCVs()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun createProgressView(context: Context?, maximum: Int) =
        ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            progress = 0
            if (maximum > 0) max = maximum
            else isIndeterminate = true
            val p = resources.getDimension(R.dimen.dialog_padding)
            setPadding(p.toInt())
        }

    private fun readAllCVs() {
        val maxCvs = Lp5MappingViewModel.ROWS * Lp5MappingViewModel.COLS
        val progressView = createProgressView(context, maxCvs)

        var job: Job? = null
        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.title_dialog_reading_x_cvs, maxCvs))
            .setView(progressView)
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                job?.cancel()
//                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            .show()

        model.setLoaded(false)

        job = lifecycleScope.launch {
            try {
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
                        progressView.incrementProgressBy(1)
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
                        progressView.incrementProgressBy(1)
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
        val progressView = createProgressView(context, 0)

        var job: Job? = null
        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.title_dialog_reading_cvs))
            .setView(progressView)
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                job?.cancel()
//                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            .show()

        model.setLoaded(false)

        job = lifecycleScope.launch {
            try {
                var idx = 0
                for (ri in model.rowIndexes) {
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
            if (value < 0) throw Exception("Failed to write CV $cv")
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
            if (value < 0) throw Exception("Failed to read CV $cv")
            cont.resume(value)
        }
    }

//    private fun createEditRowDialog(): View {
//        val view = ScrollView(context)
//        val layout = LinearLayout(context).apply {
//            orientation = LinearLayout.VERTICAL
//            val p = resources.getDimension(R.dimen.dialog_padding)
//            setPadding(p.toInt())
//        }
//        view.addView(layout)
//
//        val m = resources.getDimension(R.dimen.text_margin)
//
//        for (ci in model.inputColumnIndexes + model.outputColumnIndexes) {
//            val cvValue = model.getEditRowValue(ci)
//
//            val textView = TextView(context).apply {
//                val letter = Lp5MappingViewModel.CONTROL_CV_LETTERS[ci]
//                text = context.getString(R.string.label_lp5_control_cv_x, letter)
//            }
//            layout.addView(textView)
//
//            val stringArrayId = Lp5MappingViewModel.CONTROL_CV_STRING_ID[ci];
//            model.getStringList(context!!, stringArrayId).forEachIndexed { idx, str ->
//                val switchView = Switch(context).apply {
//                    text = str
//                    if (cvValue > 0) {
//                        isChecked = (cvValue and 2f.pow(idx).toInt() == cvValue)
//                    }
//                    setPadding(m.toInt())
//                    setOnCheckedChangeListener { _, chk ->
//                        val oldValue = model.getEditRowValue(ci)
//                        val newValue =
//                            if (chk) oldValue or 2f.pow(idx).toInt()
//                            else oldValue and 2f.pow(idx).toInt().inv()
//                        model.setEditRowValue(ci, newValue)
//                    }
//                }
//                layout.addView(switchView)
//            }
//        }
//
//        return view
//    }

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
                text = "Inputs"
            })
            addTab(newTab().apply {
                text = "Outputs"
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
            model.getStringList(context, stringArrayId).forEachIndexed { idx, str ->
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
        val progressView = createProgressView(context, Lp5MappingViewModel.COLS)

        var job: Job? = null
        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.title_dialog_writing_cvs))
            .setView(progressView)
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                job?.cancel()
//                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            .show()

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
                    progressView.incrementProgressBy(1)
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