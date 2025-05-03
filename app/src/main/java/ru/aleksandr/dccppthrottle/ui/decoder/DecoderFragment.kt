/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore
import kotlin.coroutines.resume

open class DecoderFragment : Fragment() {

    private val TAG = javaClass.simpleName

    protected suspend fun readCv(cv: Int, mock: (() -> Int)? = null): Int = suspendCancellableCoroutine { cont ->
        if (BuildConfig.DEBUG && !CommandStation.isConnected() && mock !== null) {
            val value = mock.invoke()
            Log.d(TAG, "Fake read CV $cv = $value")
            Handler(Looper.getMainLooper()).postDelayed({
                cont.resume(value)
            }, 100)
        }
        else CommandStation.getCvProg(cv) { _, value ->
            if (value < 0) {
                val ex = Exception(getString(R.string.message_read_cv_error, cv))
                cont.cancel(ex)
            }
            else cont.resume(value)
        }
    }

    protected suspend fun writeCv(cv: Int, value: Int): Boolean = suspendCancellableCoroutine { cont ->
        if (BuildConfig.DEBUG && !CommandStation.isConnected()) {
            Log.d(TAG, "Fake write CV $cv = $value")
            Handler(Looper.getMainLooper()).postDelayed({
                cont.resume(true)
            }, 200)
        }
        else CommandStation.setCvProg(cv, value) { _, value ->
            if (value < 0) {
                val ex = Exception(getString(R.string.message_write_cv_error, cv))
                cont.cancel(ex)
            }
            else cont.resume(value >= 0)
        }
    }

    private suspend fun checkManufacturer(compatibleIds: List<Int>) {
        val cv8 = readCv(MANUFACTURER_CV, MockStore::randomDecoderManufacturer)
        if (!compatibleIds.contains(cv8)) {
            val message = getString(R.string.message_wrong_manufacturer, cv8)
            if (BuildConfig.DEBUG) Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            else throw Exception(message)
        }
    }
    protected suspend fun checkManufacturer(vararg compatibleIds: Int) {
        return checkManufacturer(compatibleIds.toList())
    }

    protected fun readModelCVs(model: CvListModel, vararg manufacturerIds: Int) {
        val maxCvs = model.cvNumbers.size
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
                checkManufacturer(manufacturerIds.toList())

                for (cv in model.cvNumbers) {
                    val value = readCv(cv, MockStore::randomByteCvValue)
                    model.setCvValue(cv, value, true)
                    dialog.incrementProgress()
                }
                model.setLoaded(true)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    if (BuildConfig.DEBUG) Log.w(TAG, e)
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
                if (BuildConfig.DEBUG) {
                    model.setLoaded(true)
                }
            }
            dialog.dismiss()
        }
    }

    protected fun writeChangedCVs(model: CvListModel): Job {
        val changes = model.getChanges()
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setTitle(R.string.title_dialog_writing_cvs)
            setMax(changes.size)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            show()
        }

        job = lifecycleScope.launch {
            try {
                changes.forEach {
                    writeCv(it.key, it.value)
                    dialog.incrementProgress()
                }
                model.commitChanges()
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
        return job
    }

    companion object {
        const val MANUFACTURER_CV = 8
        const val MANUFACTURER_ID_ESU = 151
        const val MANUFACTURER_ID_PIKO = 162
        const val MANUFACTURER_ID_UHLENBROCK = 85
    }
}