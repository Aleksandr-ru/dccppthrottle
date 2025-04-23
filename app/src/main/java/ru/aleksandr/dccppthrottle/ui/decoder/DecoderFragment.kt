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
import androidx.fragment.app.Fragment
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.cs.CommandStation
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
}