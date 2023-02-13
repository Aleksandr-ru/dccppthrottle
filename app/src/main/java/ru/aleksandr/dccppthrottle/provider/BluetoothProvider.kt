package ru.aleksandr.dccppthrottle.provider

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context

object BluetoothProvider {

    private lateinit var btAdaper : BluetoothAdapter

    var initialized = false
        get() = ::btAdaper.isInitialized
        private set

    fun init(context: Context) {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdaper = btManager.adapter
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        return btAdaper.bondedDevices
    }
}