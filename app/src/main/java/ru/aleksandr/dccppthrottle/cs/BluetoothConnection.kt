package ru.aleksandr.dccppthrottle.cs

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothConnection(context: Context) : Closeable {
    private val TAG = javaClass.simpleName

    private val handler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MESSAGE_CONNECTED -> {
                connectListener?.invoke()
                true
            }
            MESSAGE_CONNECT_FAILED -> {
                val exception = msg.obj as Throwable
                failListener?.invoke(exception)
                true
            }
            MESSAGE_READ -> {
                val message = msg.obj.toString()
                receiveListener?.invoke(message)
                true
            }
            MESSAGE_WRITE -> {
                val message = msg.obj.toString()
                Log.i(TAG, "Sent: $message")
                true
            }
            else -> throw BluetoothConnectionException("Unknown message type ${msg.what}")
        }
    }
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private var btAdaper : BluetoothAdapter
    private var address : String? = null

    private var connectListener: (() -> Unit)? = null
    private var receiveListener: ((message: String) -> Unit)? = null
    private var failListener: ((ex: Throwable) -> Unit)? = null

    init {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdaper = btManager.adapter
        if (!btAdaper.isEnabled) {
            throw BluetoothConnectionException("Bluetooth adapter disabled")
        }
    }

    fun setOnConnectListener(listener : (() -> Unit)?) {
        connectListener = listener
    }

    fun setOnReceiveListener(listener: ((message: String) -> Unit)?) {
        receiveListener = listener
    }

    fun setOnFailListener(listener: ((ex: Throwable) -> Unit)?) {
        failListener = listener
    }

    fun getAddress() : String? {
        return address
    }

    fun connect(mac: String) {
        val device = btAdaper.getRemoteDevice(mac)
        connectThread = ConnectThread(device)
        connectThread!!.start()
        address = mac
    }

    override fun close() {
        connectedThread?.cancel()
        connectedThread = null
        connectThread?.cancel()
        connectThread = null
        address = null
    }

    fun send(message: String) {
        connectedThread!!.write(message)
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            val uuid = device.uuids.first().uuid
            device.createRfcommSocketToServiceRecord(uuid)
        }

        override fun run() {
            // btAdaper.cancelDiscovery() // Need BLUETOOTH ADMIN permission
            mmSocket?.let { socket ->
                try {
                    socket.connect()
                    handler.obtainMessage(MESSAGE_CONNECTED).sendToTarget()
                }
                catch (e: IOException) {
                    Log.e(TAG, "Connect failed", e)
                    handler.obtainMessage(MESSAGE_CONNECT_FAILED, e).sendToTarget()
                    return@run
                }

                connectedThread = ConnectedThread(socket)
                connectedThread!!.start()
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            }
            catch (e : IOException) {
                Log.e(TAG, "Could not close the client socket", e)
                handler.obtainMessage(MESSAGE_CONNECT_FAILED, e).sendToTarget()
            }
        }
    }

    private inner class ConnectedThread(
        private val mmSocket: BluetoothSocket
    ) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(BUFFER_SIZE) // mmBuffer store for the stream

        override fun run() {
            var numBytes = 0

            while (true) {
                try {
                    mmBuffer[numBytes] = mmInStream.read().toByte()
                    if (mmBuffer[numBytes].toInt().toChar() == '\n') {
                        val message = String(mmBuffer, 0, numBytes)
                        Log.i(TAG, "Got message: $message")
                        handler.obtainMessage(MESSAGE_READ, message).sendToTarget()
                        numBytes = 0
                    }
                    else numBytes++
                }
                catch (e: IOException) {
                    Log.i(TAG, "Input stream was disconnected", e)
                    handler.obtainMessage(MESSAGE_CONNECT_FAILED, e).sendToTarget()
                    break
                }

//                numBytes = try {
//                    mmInStream.read(mmBuffer)
//                }
//                catch (e: IOException) {
//                    Log.i(TAG, "Input stream was disconnected", e)
//                    break
//                }
//
//                val message = String(mmBuffer, 0, numBytes)
//                Log.i(TAG, "Got message: $message")
//                message.trim().split("\n").forEach {
//                    handler.obtainMessage(MESSAGE_READ, it).sendToTarget()
//                }
            }
        }

        fun write(message: String) {
            val buffer : ByteArray = message.toByteArray()
            try {
                mmOutStream.write(buffer)
                handler.obtainMessage(MESSAGE_WRITE, message).sendToTarget()
            }
            catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                handler.obtainMessage(MESSAGE_CONNECT_FAILED, e).sendToTarget()
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            }
            catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
                handler.obtainMessage(MESSAGE_CONNECT_FAILED, e).sendToTarget()
            }
        }
    }

    inner class BluetoothConnectionException(message: String) : Exception(message)

    companion object {
        const val BUFFER_SIZE = 1024

        const val MESSAGE_READ = 1
        const val MESSAGE_WRITE = 2
        const val MESSAGE_CONNECTED = 3
        const val MESSAGE_CONNECT_FAILED = 4
    }
}