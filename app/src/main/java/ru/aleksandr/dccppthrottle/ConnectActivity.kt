package ru.aleksandr.dccppthrottle

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import ru.aleksandr.dccppthrottle.cs.BluetoothConnection
import ru.aleksandr.dccppthrottle.cs.CommandStation

class ConnectActivity : AppCompatActivity() {

    private val bluetoothPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT
        else Manifest.permission.BLUETOOTH

    private val bluetoothRequest = 1

    private var pairedDevices : Set<BluetoothDevice>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (CommandStation.isConnected()) {
            CommandStation.disconnect()
        }

        setContentView(R.layout.activity_connect)

        val ver = findViewById<TextView>(R.id.textViewVersion)
        ver.text = String.format(
            getString(R.string.app_version),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )

        val layout = findViewById<ConstraintLayout>(androidx.constraintlayout.widget.R.id.layout)
        val btn = findViewById<Button>(R.id.btnConnect)
        btn.setOnClickListener {
            val spinner: Spinner = findViewById(R.id.spinnerBtList)
            val deviceName : String = pairedDevices?.elementAtOrNull(spinner.selectedItemPosition)?.name ?: "UNKNOWN" // TODO delete me

            val message = String.format(getString(R.string.message_connecting_to), deviceName)
            val snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE)
            snackbar.show()
            btn.isEnabled = false

            Handler().postDelayed({
                val myIntent = Intent(this, MainActivity::class.java)
                startActivity(myIntent)

                snackbar.dismiss()
            }, 1000)

//            btn.isEnabled = false
//            val spinner: Spinner = findViewById(R.id.spinnerBtList)
//            val device = pairedDevices!!.elementAt(spinner.selectedItemPosition)
//            val message = String.format(getString(R.string.message_connecting_to), device.name)
//            val snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE)
//            snackbar.show()
//
//            val connection = BluetoothConnection(this)
//            connection.setOnConnectListener { connected ->
//                snackbar.dismiss()
//                if (connected) {
//                    CommandStation.setConnection(connection)
//                    val myIntent = Intent(this, MainActivity::class.java)
//                    startActivity(myIntent)
//                }
//                else {
//                    Toast.makeText(this, R.string.message_connect_failed, Toast.LENGTH_SHORT).show()
//                    btn.isEnabled = true
//                }
//            }
//            connection.connect(device.address)
        }

        if (checkSelfPermission(bluetoothPermission) == PackageManager.PERMISSION_GRANTED) {
            setupDevicesList()
        }
        else if (shouldShowRequestPermissionRationale(bluetoothPermission)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_alert_permission_required))
                .setMessage(getString(R.string.message_bluetooth_permission))
                .setPositiveButton(R.string.label_ok) { dialog, _ ->
                    dialog.dismiss()
                    requestPermissions(arrayOf(bluetoothPermission), bluetoothRequest)
                }
                .create().show()
        }
        else {
            requestPermissions(arrayOf(bluetoothPermission), bluetoothRequest)
        }
    }

    private fun setupDevicesList() {
        val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val btAdaper = btManager.adapter
        if (!btAdaper.isEnabled) {
            Toast.makeText(this, R.string.message_bluetooth_disabled, Toast.LENGTH_SHORT).show()
        }
        pairedDevices = btAdaper.bondedDevices

        val btn = findViewById<Button>(R.id.btnConnect)
        val spinner: Spinner = findViewById(R.id.spinnerBtList)
        var devicesList = listOf(getString(R.string.label_bluetooth_empty))
        if (pairedDevices.isNullOrEmpty()) {
            // Disable or enable it before setting the adapter.
            spinner.isEnabled = false
            btn.isEnabled = false
        }
        else {
            btn.isEnabled = true
            spinner.isEnabled = true
            devicesList = pairedDevices!!.map { it.name }
        }
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            devicesList
        )
        spinner.adapter = adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            bluetoothRequest -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    setupDevicesList()
                }
                else {
                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    val layout = findViewById<ConstraintLayout>(androidx.constraintlayout.widget.R.id.layout)
                    val message = getString(R.string.message_bluetooth_denied)
                    Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.label_quit) { finishAndRemoveTask() }
                        .show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}