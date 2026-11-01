/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.cs

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import ru.aleksandr.dccppthrottle.BuildConfig
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

class BluetoothConnection(context: Context) : Closeable {
    @Suppress("unused")
    private val TAG = javaClass.simpleName
    private val handler = Handler(Looper.getMainLooper())
    private val lock = Any()
    private var session: Session? = null
    private var address: String? = null
    private val btAdapter: BluetoothAdapter

    private var connectListener: (() -> Unit)? = null
    private var receiveListener: ((message: String) -> Unit)? = null
    private var failListener: ((ex: Throwable) -> Unit)? = null

    init {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        if (!btAdapter.isEnabled) {
            throw BluetoothConnectionException("Bluetooth adapter disabled")
        }
    }

    fun setOnConnectListener(listener: (() -> Unit)?) {
        connectListener = listener
    }

    fun setOnReceiveListener(listener: ((message: String) -> Unit)?) {
        receiveListener = listener
    }

    fun setOnFailListener(listener: ((ex: Throwable) -> Unit)?) {
        failListener = listener
    }

    fun getAddress(): String? = synchronized(lock) { address }

    fun connect(mac: String) {
        val device = btAdapter.getRemoteDevice(mac)
        close()
        synchronized(lock) {
            val next = Session(device)
            session = next
            address = mac
            next.connectThread.start()
        }
    }

    override fun close() {
        val previous = synchronized(lock) {
            val previous = session
            session = null
            address = null
            previous
        }
        previous?.cancel()
    }

    fun send(message: String) {
        synchronized(lock) { session }?.send(message)
    }

    private inner class Session(private val device: BluetoothDevice) {
        private var socket: BluetoothSocket? = null
        private var stopped = false
        private var ready = false
        private val writer = Executors.newSingleThreadExecutor { task ->
            Thread(task, "DCC-Bluetooth-write")
        }
        val connectThread = Thread({ connectSocket() }, "DCC-Bluetooth-connect")
        private var reader: Thread? = null

        // Check the session again on delivery: queued events from an old connection
        // must not affect a newly connected station or report a deliberate close.
        private fun postEvent(action: () -> Unit) {
            handler.post {
                val active = synchronized(lock) { session === this && !stopped }
                if (active) action()
            }
        }

        @SuppressLint("MissingPermission")
        private fun connectSocket() {
            try {
                val uuid = device.uuids?.firstOrNull()?.uuid
                    ?: throw IOException("Bluetooth device has no service UUID")
                val newSocket = device.createRfcommSocketToServiceRecord(uuid)
                synchronized(lock) {
                    if (stopped || session !== this) {
                        closeSocket(newSocket)
                        return
                    }
                    socket = newSocket
                }
                newSocket.connect()
                val input = newSocket.inputStream
                val output = newSocket.outputStream
                synchronized(lock) {
                    if (stopped || session !== this) return
                    writeMessage = { message -> output.write(message.toByteArray(Charsets.UTF_8)) }
                    ready = true
                    reader = Thread({
                        try {
                            val buffer = ByteArray(BUFFER_SIZE)
                            val lines = BluetoothLineBuffer(BUFFER_SIZE)
                            while (!Thread.currentThread().isInterrupted) {
                                val count = input.read(buffer)
                                if (count < 0) throw IOException("Bluetooth input stream closed")
                                lines.append(buffer, count) { message ->
                                    if (BuildConfig.DEBUG) Log.i(TAG, "Got message: $message")
                                    postEvent { receiveListener?.invoke(message) }
                                }
                            }
                        } catch (e: IOException) {
                            fail(e)
                        }
                    }, "DCC-Bluetooth-read")
                    // Both streams and the write queue are ready before notifying the UI.
                    postEvent { connectListener?.invoke() }
                    reader!!.start()
                }
            } catch (e: Exception) {
                fail(e)
            }
        }

        private var writeMessage: ((String) -> Unit)? = null

        fun send(message: String) {
            synchronized(lock) {
                if (stopped || session !== this || !ready) return
                try {
                    writer.execute {
                        try {
                            val write = synchronized(lock) {
                                if (stopped || session !== this) null else writeMessage
                            } ?: return@execute
                            write(message)
                            if (BuildConfig.DEBUG) Log.i(TAG, "Sent: $message")
                        } catch (e: IOException) {
                            fail(e)
                        }
                    }
                } catch (_: RejectedExecutionException) {
                    // The connection was closed while this send was being queued.
                }
            }
        }

        private fun fail(ex: Throwable) {
            synchronized(lock) {
                if (stopped || session !== this) return
                stopped = true
                ready = false
                address = null
            }
            releaseResources()
            if (BuildConfig.DEBUG) Log.w(TAG, "Bluetooth connection failed", ex)
            handler.post {
                if (synchronized(lock) { session === this }) failListener?.invoke(ex)
            }
        }

        fun cancel() {
            synchronized(lock) {
                stopped = true
                ready = false
            }
            releaseResources()
        }

        private fun releaseResources() {
            writer.shutdownNow()
            connectThread.interrupt()
            reader?.interrupt()
            // Closing the socket unblocks both connect() and read()/write().
            val toClose = synchronized(lock) { socket }
            toClose?.let { closeSocket(it) }
        }
    }

    private fun closeSocket(socket: BluetoothSocket) {
        try {
            socket.close()
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Could not close Bluetooth socket", e)
        }
    }

    inner class BluetoothConnectionException(message: String) : Exception(message)

    companion object {
        const val BUFFER_SIZE = 1024
    }
}
