package ru.aleksandr.dccppthrottle.cs

import java.io.IOException
import java.util.ArrayDeque
import java.util.concurrent.TimeoutException

class RequestHandle internal constructor(private val cancelAction: () -> Unit) {
    fun cancel() = cancelAction()
}

internal class RequestTimeoutException(val command: String, val programming: Boolean) : TimeoutException("No response to $command")
internal class RequestDisconnectedException : IOException("Command station disconnected")
internal class RequestQueueBlockedException : IOException("Reconnect before programming CVs again")
internal class RequestQueueFullException : IOException("Too many pending requests")

/** All state, including timer callbacks, is confined to [dispatch]'s thread. */
internal class StationRequestQueue(
    private val send: (String) -> Unit,
    private val dispatch: (() -> Unit) -> Unit,
    private val schedule: (Long, () -> Unit) -> (() -> Unit),
    private val stopOnTimeout: Boolean,
    private val maxPending: Int = 64
) {
    private class Request(
        val command: String,
        val response: Regex,
        val timeoutMs: Long,
        var onResult: ((List<String>) -> Unit)?,
        var onError: ((Exception) -> Unit)?
    ) {
        var cancelled = false
        var cancelTimer: (() -> Unit)? = null

        fun clearCallbacks() {
            onResult = null
            onError = null
        }
    }

    private val pending = ArrayDeque<Request>()
    private var active: Request? = null
    private var closed = false
    private var blocked = false

    fun submit(
        command: String,
        response: Regex,
        timeoutMs: Long,
        onError: (Exception) -> Unit,
        onResult: (List<String>) -> Unit
    ): RequestHandle {
        val request = Request(command, response, timeoutMs, onResult, onError)
        dispatch {
            when {
                closed -> reject(request, RequestDisconnectedException())
                blocked -> reject(request, RequestQueueBlockedException())
                pending.size + (if (active == null) 0 else 1) >= maxPending ->
                    reject(request, RequestQueueFullException())
                else -> {
                    pending.addLast(request)
                    startNext()
                }
            }
        }
        return RequestHandle {
            dispatch {
                request.cancelled = true
                request.clearCallbacks()
                pending.remove(request)
                // An already sent operation still occupies the programming track.
                // Drain its reply before sending the next one, even after UI cancellation.
            }
        }
    }

    fun receive(message: String) = dispatch {
        val request = active ?: return@dispatch
        val match = request.response.matchEntire(message) ?: return@dispatch
        active = null
        request.cancelTimer?.invoke()
        request.cancelTimer = null
        val callback = request.onResult
        request.clearCallbacks()
        try {
            if (!request.cancelled) callback?.invoke(match.groupValues)
        } finally {
            startNext()
        }
    }

    fun close() = dispatch {
        closed = true
        blocked = false
        val requests = detachAll()
        requests.forEach { reject(it, RequestDisconnectedException()) }
    }

    private fun startNext() {
        if (closed || blocked || active != null || pending.isEmpty()) return
        val request = pending.removeFirst()
        active = request
        request.cancelTimer = schedule(request.timeoutMs) {
            if (active === request) {
                active = null
                request.cancelTimer = null
                // CV writes have no request ID. After an uncertain timeout, do not
                // let a delayed reply complete another operation in this session.
                if (stopOnTimeout) blocked = true
                val waiting = if (stopOnTimeout) detachAll() else emptyList()
                try {
                    reject(request, RequestTimeoutException(request.command, stopOnTimeout))
                    waiting.forEach { reject(it, RequestQueueBlockedException()) }
                } finally {
                    startNext()
                }
            }
        }
        try {
            send(request.command)
        } catch (e: Exception) {
            closed = true
            val requests = detachAll()
            requests.forEach { reject(it, e) }
        }
    }

    private fun detachAll(): List<Request> {
        val requests = mutableListOf<Request>()
        active?.let { requests.add(it) }
        active = null
        requests.addAll(pending)
        pending.clear()
        requests.forEach {
            it.cancelTimer?.invoke()
            it.cancelTimer = null
        }
        return requests
    }

    private fun reject(request: Request, error: Exception) {
        val callback = request.onError
        request.clearCallbacks()
        if (!request.cancelled) callback?.invoke(error)
    }
}
