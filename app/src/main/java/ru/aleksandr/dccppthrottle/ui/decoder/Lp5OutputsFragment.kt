/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.DecoderActivity
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore
import kotlin.Exception
import kotlin.coroutines.resume

class Lp5OutputsFragment : Fragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Lp5OutputsViewModel>()
    private lateinit var rvAdapter: Lp5OutputsRecyclerViewAdapter

    private lateinit var emptyView: TextView
    private lateinit var listView: RecyclerView

    companion object {
        fun newInstance() = Lp5OutputsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lp5_outputs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            emptyView = findViewById(R.id.empty_view)
            listView = findViewById(R.id.rv_lp5_outputs)
        }

        emptyView.setOnClickListener {
            readAllCVs()
        }

        rvAdapter = Lp5OutputsRecyclerViewAdapter(model)
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
//            if (it != null) AlertDialog.Builder(context)
//                .setTitle(getString(
//                    R.string.title_dialog_lp5_row_x,
//                    it + 1
//                ))
//                .setView(createEditRowDialog())
//                .setPositiveButton(R.string.action_write) { _, _ -> writeEditRowCVs() }
//                .setNegativeButton(android.R.string.cancel, null)
//                .show()
            Toast.makeText(context, "Outputs dialog not implemented", Toast.LENGTH_SHORT).show()
        }

        if ((savedInstanceState == null) && (model.loaded.value == true)) {
            Snackbar.make(view, R.string.message_cvs_outdated, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_reload) {
                    readAllCVs()
                }.show()
        }
    }

    private fun readAllCVs() {
        val maxCvs = Lp5OutputsViewModel.ROWS * Lp5OutputsViewModel.COLS
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
                val cv8 = readCv(DecoderActivity.MANUFACTURER_CV)
                if (cv8 != DecoderActivity.MANUFACTURER_ID_ESU) {
                    val message = getString(R.string.message_wrong_manufacturer, cv8)
                    if (BuildConfig.DEBUG) Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    else throw Exception(message)
                }

                writeIndexCvs()

                for (ri in model.rowIndexes) {
                    for (ci in model.columnIndexes) {
                        val cv = model.cvNumber(ri, ci)
                        val value = readCv(cv)
                        if (BuildConfig.DEBUG) Log.d(TAG,
                            "Row $ri, col $ci, CV $cv = $value"
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
            val value = MockStore.randomLp5OutputCvValue()
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

    private suspend fun writeIndexCvs() {
        writeCv(Lp5OutputsViewModel.INDEX_CV1.first, Lp5OutputsViewModel.INDEX_CV1.second)
        writeCv(Lp5OutputsViewModel.INDEX_CV2.first, Lp5OutputsViewModel.INDEX_CV2.second)
    }

    private fun writeEditRowCVs() {
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setTitle(R.string.title_dialog_writing_cvs)
            setMax(Lp5OutputsViewModel.COLS)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            show()
        }

        job = lifecycleScope.launch {
            try {
                writeIndexCvs()

                val rowIndex = model.editRowIndex.value!!
                for (ci in model.columnIndexes) {
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
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
                job?.cancel()
                model.setLoaded(false)
            }
            dialog.dismiss()
        }
    }
}