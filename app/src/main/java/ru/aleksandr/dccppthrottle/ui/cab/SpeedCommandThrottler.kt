/*
 * Copyright (c) 2026. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.cab

import android.os.Handler
import android.os.Looper

/** One periodic timer for one slider gesture. All calls run on the UI thread. */
internal class SpeedCommandThrottler(
    private val send: (Int) -> Unit,
    private val intervalMs: Long = 250L
) {
    private val handler = Handler(Looper.getMainLooper())
    private var speed: Int? = null
    private var lastSent: Int? = null
    private var active = true
    private val timer = object : Runnable {
        override fun run() {
            if (!active) return
            emit()
            if (active) handler.postDelayed(this, intervalMs)
        }
    }

    init {
        handler.postDelayed(timer, intervalMs)
    }

    fun submit(value: Int) {
        if (!active) return
        speed = value
        if (value == 0) emit()
    }

    fun finish(value: Int) {
        if (!active) return
        cancel()
        speed = value
        emit()
    }

    fun cancel() {
        active = false
        handler.removeCallbacks(timer)
    }

    private fun emit() {
        val value = speed ?: return
        if (value == lastSent) return
        lastSent = value
        send(value)
    }
}
