/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore
import ru.aleksandr.dccppthrottle.ui.decoder.DecoderFragment
import ru.aleksandr.dccppthrottle.view.ByteSwitchView
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
                    .setView(createEditRowDialog())
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

    private fun createEditRowDialog(): View {
        val view = layoutInflater.inflate(R.layout.dialog_xp5_output, null)
        with(view) {
            val scrollView = findViewById<ScrollView>(R.id.scrollView)
            val tabsView = findViewById<TabLayout>(R.id.tabLayout)
            val spinnerView = findViewById<Spinner>(R.id.spinnerEffect)
            val effectDescView = findViewById<TextView>(R.id.textEffectDesc)
            val pwmView = findViewById<PlusMinusView>(R.id.plusminusPwm)
            val flagsView = findViewById<ByteSwitchView>(R.id.byteFlags)
            val param1View = findViewById<PlusMinusView>(R.id.plusminusParam1)
            val param1DescView = findViewById<TextView>(R.id.textParam1Desc)
            val param2View = findViewById<PlusMinusView>(R.id.plusminusParam2)
            val param2DescView = findViewById<TextView>(R.id.textParam2Desc)

            var effectA = false

            val effects = model.getEffectsMap(context)
            val effectsDesc = resources.getStringArray(R.array.xp5_output_effects_desc)
            val paramsDesc = resources.getStringArray(R.array.xp5_output_effects_param_desc)
            spinnerView.adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                effects.values.toTypedArray()
            )
            spinnerView.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    effectDescView.text = effectsDesc.getOrElse(position) { "" }
                    param1DescView.text = paramsDesc[position * 2]
                    param2DescView.text = paramsDesc[position * 2 + 1]

                    val cvValue = effects.keys.elementAt(position)
                    if (effectA) model.setEditRowValue(Xp5OutputsViewModel.COL_EFFECTA, cvValue)
                    else model.setEditRowValue(Xp5OutputsViewModel.COL_EFFECTB, cvValue)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // do nothing
                }

            }

            tabsView.selectTab(tabsView.getTabAt(1))
            tabsView.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (!effectA) {
                        effectA = true
                        spinnerView.setSelection(
                            effects.keys.indexOf(
                                model.getEditRowValue(
                                    Xp5OutputsViewModel.COL_EFFECTA
                                )
                            ).takeIf { it >= 0 } ?: 0
                        )
                        pwmView.value = model.getEditRowValue(Xp5OutputsViewModel.COL_PWMA)
                        flagsView.value = model.getEditRowValue(Xp5OutputsViewModel.COL_FLAGSA)
                        param1View.value = model.getEditRowValue(Xp5OutputsViewModel.COL_PARAM1A)
                        param2View.value = model.getEditRowValue(Xp5OutputsViewModel.COL_PARAM2A)
                    }
                    else {
                        effectA = false
                        spinnerView.setSelection(
                            effects.keys.indexOf(
                                model.getEditRowValue(
                                    Xp5OutputsViewModel.COL_EFFECTB
                                )
                            ).takeIf { it >= 0 } ?: 0
                        )
                        pwmView.value = model.getEditRowValue(Xp5OutputsViewModel.COL_PWMB)
                        flagsView.value = model.getEditRowValue(Xp5OutputsViewModel.COL_FLAGSB)
                        param1View.value = model.getEditRowValue(Xp5OutputsViewModel.COL_PARAM1B)
                        param2View.value = model.getEditRowValue(Xp5OutputsViewModel.COL_PARAM2B)
                    }

                    scrollView.children.forEach {
                        if (it.hasFocus()) it.clearFocus()
                    }
                    scrollView.scrollTo(0, 0)
                    // https://stackoverflow.com/a/62061927
                    val manager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    manager?.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // do nothing
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // do nothing
                }
            })
            tabsView.selectTab(tabsView.getTabAt(0))

            pwmView.setOnChangeListener {
                if (it != null) {
                    if (effectA) model.setEditRowValue(Xp5OutputsViewModel.COL_PWMA, it)
                    else model.setEditRowValue(Xp5OutputsViewModel.COL_PWMB, it)
                }
            }

            flagsView.setOnChangeListener {
                if (effectA) model.setEditRowValue(Xp5OutputsViewModel.COL_FLAGSA, it)
                else model.setEditRowValue(Xp5OutputsViewModel.COL_FLAGSB, it)
            }

            param1View.setOnChangeListener {
                if (it != null) {
                    if (effectA) model.setEditRowValue(Xp5OutputsViewModel.COL_PARAM1A, it)
                    else model.setEditRowValue(Xp5OutputsViewModel.COL_PARAM1B, it)
                }
            }

            param2View.setOnChangeListener {
                if (it != null) {
                    if (effectA) model.setEditRowValue(Xp5OutputsViewModel.COL_PARAM2A, it)
                    else model.setEditRowValue(Xp5OutputsViewModel.COL_PARAM2B, it)
                }
            }

        }
        return view!!
    }

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