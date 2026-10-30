package ru.aleksandr.dccppthrottle.cs

import android.content.Context
import ru.aleksandr.dccppthrottle.R

internal fun Context.stationRequestError(error: Exception): String = when (error) {
    is RequestTimeoutException -> getString(R.string.message_request_timeout, error.command) +
        if (error.programming) " " + getString(R.string.message_request_cv_blocked) else ""
    is RequestDisconnectedException -> getString(R.string.message_request_disconnected)
    is RequestQueueBlockedException -> getString(R.string.message_request_cv_blocked)
    is RequestQueueFullException -> getString(R.string.message_request_queue_full)
    else -> error.message ?: error.toString()
}
