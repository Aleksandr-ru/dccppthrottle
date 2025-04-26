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
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.cs.CommandStation
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

    protected suspend fun checkManufacturer(vararg compatibleIds: Int) {
        val cv8 = readCv(MANUFACTURER_CV, MockStore::randomDecoderManufacturer)
        if (!compatibleIds.contains(cv8)) {
            val message = getString(R.string.message_wrong_manufacturer, cv8)
            if (BuildConfig.DEBUG) Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            else throw Exception(message)
        }
    }

    companion object {
        const val MANUFACTURER_CV = 8
        const val MANUFACTURER_ID_ESU = 151
        const val MANUFACTURER_ID_PIKO = 162
        const val MANUFACTURER_ID_UHLENBROCK = 85
    }
}