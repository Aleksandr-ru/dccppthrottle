/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.app.AlertDialog
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import kotlin.Exception

class Xp5OutputsFragment : DecoderFragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Xp5OutputsViewModel>()
    private lateinit var rvAdapter: Xp5OutputsRecyclerViewAdapter

    private lateinit var emptyView: TextView
    private lateinit var listView: RecyclerView

    companion object {
        fun newInstance() = Xp5OutputsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_xp5_outputs, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            emptyView = findViewById(R.id.textEmpty)
            listView = findViewById(R.id.rvOutputs)
        }

        emptyView.setOnClickListener {
            readAllCVs()
        }

        rvAdapter = Xp5OutputsRecyclerViewAdapter(model)
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
            if (it != null) {
                val titles = resources.getStringArray(R.array.xp5_outputs)
                AlertDialog.Builder(context)
                    .setTitle(titles[it])
//                    .setView(createEditRowDialog())
                    .setPositiveButton(R.string.action_write) { _, _ -> writeEditRowCVs() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> model.editRow(null) }
                    .show()
            }
        }

        if ((savedInstanceState == null) && (model.loaded.value == true)) {
            Snackbar.make(view, R.string.message_cvs_outdated, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_reload) {
                    readAllCVs()
                }.show()
        }
    }

//    private fun createEditRowDialog(): View {
//        val view = layoutInflater.inflate(R.layout.dialog_xp5_output, null)
//        with(view) {
//            val spinnerView = findViewById<Spinner>(R.id.spinnerMode)
//            val modeDescView = findViewById<TextView>(R.id.textModeDesc)
//            val brightnessDescView = findViewById<TextView>(R.id.textBrightnessDesc)
//            val special1DescView = findViewById<TextView>(R.id.textSpecial1Desc)
//            val special2DescView = findViewById<TextView>(R.id.textSpecial2Desc)
//            val special3DescView = findViewById<TextView>(R.id.textSpecial3Desc)
//
//            val modes = resources.getStringArray(R.array.xp5_output_modes)
//            val modesDesc = resources.getStringArray(R.array.xp5_output_modes_description)
//            val specialDesc = resources.getStringArray(R.array.xp5_output_modes_special)
//
//            spinnerView.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, modes)
//            spinnerView.onItemSelectedListener = object : OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                    modeDescView.text = modesDesc[position]
//                    brightnessDescView.text = specialDesc[position * 4]
//                    special1DescView.text = specialDesc[position * 4 + 1]
//                    special2DescView.text = specialDesc[position * 4 + 2]
//                    special3DescView.text = specialDesc[position * 4 + 3]
//
//                    model.setEditRowValue(Xp5OutputsViewModel.COL_MODE, position)
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    // do nothing
//                }
//            }
//            val selectedMode = model.getEditRowValue(Xp5OutputsViewModel.COL_MODE)
//            if (selectedMode < modes.size) spinnerView.setSelection(selectedMode)
//
//            // duration counters
//
//            val swOnView = findViewById<PlusMinusView>(R.id.plusminusSwitchOn)
//            val swOffView = findViewById<PlusMinusView>(R.id.plusminusSwitchOff)
//            val autoOffView = findViewById<PlusMinusView>(R.id.plusminusAutoOff)
//
//            val swOnDescView = findViewById<TextView>(R.id.textSwOnDesc)
//            val swOffDescView = findViewById<TextView>(R.id.textSwOffDesc)
//            val autoOffDescView = findViewById<TextView>(R.id.textAutoOffDesc)
//
//            swOnView.setOnChangeListener {
//                val seconds = Xp5OutputsViewModel.UNIT_SWONOFF * (it ?: 0)
//                swOnDescView.text = getString(R.string.label_xp5_time_x_sec, seconds)
//
//                val pair = Pair(swOnView.value ?: 0, swOffView.value ?: 0)
//                model.setEditRowValue(Xp5OutputsViewModel.COL_ONOFFDELAY, model.pairToSwitchOnOff(pair))
//            }
//
//            swOffView.setOnChangeListener {
//                swOffDescView.text = if ((it ?: 0) > 0) {
//                    val seconds = Xp5OutputsViewModel.UNIT_SWONOFF * (it ?: 0)
//                    getString(R.string.label_xp5_time_x_sec, seconds)
//                }
//                else getString(R.string.label_xp5_same_as_swon_time)
//
//                val pair = Pair(swOnView.value ?: 0, swOffView.value ?: 0)
//                model.setEditRowValue(Xp5OutputsViewModel.COL_ONOFFDELAY, model.pairToSwitchOnOff(pair))
//            }
//
//            autoOffView.setOnChangeListener {
//                val value = it ?: 0
//                val seconds = Xp5OutputsViewModel.UNIT_AUTOOFF * value
//                autoOffDescView.text = getString(R.string.label_xp5_time_x_sec, seconds)
//                model.setEditRowValue(Xp5OutputsViewModel.COL_AUTOOFF, value)
//            }
//
//            // numeric values
//
//            val brightnessView = findViewById<PlusMinusView>(R.id.plusminusBrightness)
//            val special1View = findViewById<PlusMinusView>(R.id.plusminusSpecial1)
//            val special2View = findViewById<PlusMinusView>(R.id.plusminusSpecial2)
//            val special3View = findViewById<PlusMinusView>(R.id.plusminusSpecial3)
//
//            brightnessView.setOnChangeListener {
//                val value = it ?: 0
//                model.setEditRowValue(Xp5OutputsViewModel.COL_BRIGHTNESS, value)
//            }
//
//            special1View.setOnChangeListener {
//                val value = it ?: 0
//                model.setEditRowValue(Xp5OutputsViewModel.COL_SPECIAL1, value)
//            }
//
//            special2View.setOnChangeListener {
//                val value = it ?: 0
//                model.setEditRowValue(Xp5OutputsViewModel.COL_SPECIAL2, value)
//            }
//
//            special3View.setOnChangeListener {
//                val value = it ?: 0
//                model.setEditRowValue(Xp5OutputsViewModel.COL_SPECIAL3, value)
//            }
//
//            val pair = model.switchOnOffToPair(model.getEditRowValue(Xp5OutputsViewModel.COL_ONOFFDELAY))
//            swOnView.value = pair.first
//            swOffView.value = pair.second
//
//            autoOffView.value = model.getEditRowValue(Xp5OutputsViewModel.COL_AUTOOFF)
//            brightnessView.value = model.getEditRowValue(Xp5OutputsViewModel.COL_BRIGHTNESS)
//            special1View.value = model.getEditRowValue(Xp5OutputsViewModel.COL_SPECIAL1)
//            special2View.value = model.getEditRowValue(Xp5OutputsViewModel.COL_SPECIAL2)
//            special3View.value = model.getEditRowValue(Xp5OutputsViewModel.COL_SPECIAL3)
//        }
//        return view!!
//    }

    private fun readAllCVs() {
        val maxCvs = Xp5OutputsViewModel.ROWS * Xp5OutputsViewModel.COLS
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
                checkManufacturer(MANUFACTURER_ID_PIKO)
                writeIndexCvs()

                for (ri in model.rowIndexes) {
                    for (ci in model.colIndexes) {
                        val cv = model.cvNumber(ri, ci)
                        val value = readCv(cv, MockStore::randomXp5OutputCvValue)
                        if (BuildConfig.DEBUG) Log.d(TAG,
                            "Row $ri, col $ci, CV $cv = $value"
                        )
                        model.setCvValue(ri, ci, value)
                        dialog.incrementProgress()
                    }
                }
                rvAdapter.notifyDataSetChanged()
                model.setLoaded(true)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    if (BuildConfig.DEBUG) Log.w(TAG, e)
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
                if (BuildConfig.DEBUG) {
                    rvAdapter.notifyDataSetChanged()
                    model.setLoaded(true)
                }
            }
            dialog.dismiss()
        }
    }

    private suspend fun writeIndexCvs() {
        writeCv(Xp5OutputsViewModel.INDEX_CV1.first, Xp5OutputsViewModel.INDEX_CV1.second)
        writeCv(Xp5OutputsViewModel.INDEX_CV2.first, Xp5OutputsViewModel.INDEX_CV2.second)
    }

    private fun writeEditRowCVs() {
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setTitle(R.string.title_dialog_writing_cvs)
            setMax(Xp5OutputsViewModel.COLS)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            setOnDismissListener { model.editRow(null) }
            show()
        }

        job = lifecycleScope.launch {
            try {
                writeIndexCvs()

                val rowIndex = model.editRowIndex.value!!
                for (ci in model.colIndexes) {
                    val cv = model.cvNumber(rowIndex, ci)
                    val value = model.getEditRowValue(ci)
                    writeCv(cv, value)
                    model.setCvValue(rowIndex, ci, value)
                    dialog.incrementProgress()
                }
                rvAdapter.notifyItemChanged(rowIndex)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    if (BuildConfig.DEBUG) Log.w(TAG, e)
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
                model.setLoaded(false)
            }
            dialog.dismiss()
        }
    }
}