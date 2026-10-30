package ru.aleksandr.dccppthrottle.ui.cab

/** Leading/trailing throttle: keep the latest speed, without a continuously running timer. */
internal class SpeedCommandThrottler(
    private val now: () -> Long,
    private val schedule: (Long, () -> Unit) -> (() -> Unit),
    private val send: (Int) -> Unit,
    private val isValid: () -> Boolean = { true },
    private val intervalMs: Long = 250L
) {
    private var lastSpeed: Int? = null
    private var lastSentAt: Long? = null
    private var pendingSpeed: Int? = null
    private var cancelTimer: (() -> Unit)? = null
    private var timerGeneration = 0L

    fun submit(speed: Int) {
        if (!isValid()) {
            cancel()
            return
        }
        if (speed == lastSpeed) {
            pendingSpeed = null
            cancelScheduled()
            return
        }
        pendingSpeed = speed
        val elapsed = lastSentAt?.let { (now() - it).coerceAtLeast(0) } ?: intervalMs
        if (elapsed >= intervalMs || speed == 0) {
            emitPending()
        } else if (cancelTimer == null) {
            val generation = ++timerGeneration
            cancelTimer = schedule(intervalMs - elapsed) {
                if (generation == timerGeneration) emitPending()
            }
        }
    }

    /** On release, the final value must not wait for the throttle interval. */
    fun flush(speed: Int) {
        pendingSpeed = speed
        emitPending()
    }

    fun cancel() {
        cancelScheduled()
        pendingSpeed = null
        lastSpeed = null
        lastSentAt = null
    }

    private fun emitPending() {
        cancelScheduled()
        val speed = pendingSpeed ?: return
        pendingSpeed = null
        if (!isValid() || speed == lastSpeed) return
        lastSpeed = speed
        lastSentAt = now()
        send(speed)
    }

    private fun cancelScheduled() {
        timerGeneration++
        cancelTimer?.invoke()
        cancelTimer = null
    }
}
