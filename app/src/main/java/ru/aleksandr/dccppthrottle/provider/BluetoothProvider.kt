package ru.aleksandr.dccppthrottle.provider

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object BluetoothProvider {

    const val MESSAGE_READ = 1
    const val MESSAGE_WRITE = 2
    const val MESSAGE_CONNECTED = 3
    const val MESSAGE_CONNECT_FAILED = 4

    private val TAG = javaClass.simpleName

    private lateinit var btAdaper : BluetoothAdapter

    private val handler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            MESSAGE_CONNECTED -> {
                connected = true
                connectListener?.invoke(connected)
                true
            }
            MESSAGE_CONNECT_FAILED -> {
                connected = false
                connectListener?.invoke(connected)
                true
            }
            MESSAGE_READ -> {
                val message = msg.obj.toString()
                receiveListener?.invoke(message)
                true
            }
            MESSAGE_WRITE -> {
                val message = msg.obj.toString()
                Log.d(TAG, "Sent: $message")
                true
            }
            else -> throw Throwable("Unknown message type ${msg.what}")
        }
    }
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private var initialized = false
    private var connected = false

    var connectListener: ((connected: Boolean) -> Boolean)? = null
    var receiveListener: ((message: String) -> Boolean)? = null

    fun init(context: Context) {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdaper = btManager.adapter
        if (!btAdaper.isEnabled) {
            initialized = false
            throw BluetoothProviderAdapterDisabledException()
        }
        initialized = true
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        if (!initialized) throw BluetoothProviderNotInitializedException()
        return btAdaper.bondedDevices
    }

    fun connect(mac: String) {
        if (!initialized) throw BluetoothProviderNotInitializedException()
        val device = btAdaper.getRemoteDevice(mac)
        connectThread = ConnectThread(device)
        connectThread!!.start()
    }

    fun disconnect() {
        if (connected) {
            connectedThread?.cancel()
            connectThread = null
        }
        connectThread?.cancel()
        connectThread = null
    }

    fun send(message: String) {
        if (!initialized) throw BluetoothProviderNotInitializedException()
        else if (!connected) throw BluetoothProviderNotConnectedException()
        connectedThread!!.write(message)
    }

    private class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            val uuid = device.uuids.first().uuid
            device.createRfcommSocketToServiceRecord(uuid)
        }

        override fun run() {
            btAdaper.cancelDiscovery()
            mmSocket?.let { socket ->
                try {
                    socket.connect()
                    handler.obtainMessage(MESSAGE_CONNECTED).sendToTarget()
                }
                catch (e: IOException) {
                    Log.e(TAG, "Connect failed", e)
                    handler.obtainMessage(MESSAGE_CONNECT_FAILED).sendToTarget()
                }

                connectedThread = ConnectedThread(socket)
                connectThread!!.start()
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            }
            catch (e : IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private class ConnectedThread(
        private val mmSocket: BluetoothSocket
    ) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes = 0

            while (true) {
//                try {
//                    mmBuffer[numBytes] = mmInStream.read().toByte()
//                    if (mmBuffer[numBytes].toInt().toChar() == '\n') {
//                        val message = String(mmBuffer, 0, numBytes - 1)
//                        Log.d(TAG, "Got message: $message")
//                        handler.obtainMessage(MESSAGE_READ, message).sendToTarget()
//                    }
//                    else numBytes++
//                }
//                catch (e: IOException) {
//                    Log.d(TAG, "Input stream was disconnected", e)
//                    break
//                }
                numBytes = try {
                    mmInStream.read(mmBuffer)
                }
                catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                val message = String(mmBuffer, 0, numBytes)
                Log.d(TAG, "Got message: $message")
                handler.obtainMessage(MESSAGE_READ, message).sendToTarget()
            }
        }

        fun write(message: String) {
            val buffer : ByteArray = message.toByteArray()
            try {
                mmOutStream.write(buffer)
            }
            catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                return
            }
            handler.obtainMessage(MESSAGE_WRITE, message).sendToTarget()
        }

        fun cancel() {
            try {
                mmSocket.close()
            }
            catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    open class BluetoothProviderException : Exception() {}
    class BluetoothProviderAdapterDisabledException : BluetoothProviderException() {}
    class BluetoothProviderNotInitializedException : BluetoothProviderException() {}
    class BluetoothProviderNotConnectedException : BluetoothProviderException() {}
}