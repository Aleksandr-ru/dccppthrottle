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
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import ru.aleksandr.dccppthrottle.cs.BluetoothConnection
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.store.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class ConnectActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName

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
        ver.text = getString(
            R.string.app_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )

        val layout = findViewById<ConstraintLayout>(androidx.constraintlayout.widget.R.id.layout)
        val btn = findViewById<Button>(R.id.btnConnect)
        val chk = findViewById<CheckBox>(R.id.checkConnectAtStart)
        btn.setOnClickListener {
            btn.isEnabled = false
            val spinner: Spinner = findViewById(R.id.spinnerBtList)
            val device = pairedDevices!!.elementAt(spinner.selectedItemPosition)
            val message = getString(R.string.message_connecting_to, device.name)
            val snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE)
            snackbar.show()

            val connection = BluetoothConnection(this)
            connection.setOnFailListener { ex ->
                Log.w(TAG, ex)
                Toast.makeText(this, R.string.message_connect_failed, Toast.LENGTH_SHORT).show()

                // https://stackoverflow.com/a/54589015
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    btn.isEnabled = true
                    snackbar.dismiss()
                }
                else {
                    // https://stackoverflow.com/questions/4038479/android-go-back-to-previous-activity
                    // https://developer.android.com/reference/android/content/Intent#FLAG_ACTIVITY_CLEAR_TOP
                    // https://developer.android.com/guide/components/activities/tasks-and-back-stack#ManagingTasks
                    val myIntent = Intent(this, ConnectActivity::class.java)
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(myIntent)
                }
            }
            connection.setOnConnectListener {
                val prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                prefsEditor.putString(PREF_LAST_MAC, connection.getAddress())
                prefsEditor.putBoolean(PREF_CONNECT, chk.isChecked)
                prefsEditor.commit()

                startCommandStation(connection, device.name)

                val myIntent = Intent(this, MainActivity::class.java)
                startActivity(myIntent)

                btn.isEnabled = true
                snackbar.dismiss()
            }
            connection.connect(device.address)
        }

        if (checkSelfPermission(bluetoothPermission) == PackageManager.PERMISSION_GRANTED) {
            setupDevicesList()
        }
        else if (shouldShowRequestPermissionRationale(bluetoothPermission)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.title_alert_permission_required)
                .setMessage(R.string.message_bluetooth_permission)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    requestPermissions(arrayOf(bluetoothPermission), bluetoothRequest)
                }
                .create().show()
        }
        else {
            requestPermissions(arrayOf(bluetoothPermission), bluetoothRequest)
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val lastMac = prefs.getString(PREF_LAST_MAC, null)
        val connectAtStart = prefs.getBoolean(PREF_CONNECT, false)
        pairedDevices?.let {
            if (connectAtStart && it.isNotEmpty() && it.elementAt(0).address == lastMac) {
                chk.isChecked = true
                btn.performClick()
            }
        }

        if (BuildConfig.DEBUG) {
            Snackbar.make(layout, "Debug mode enabled", Snackbar.LENGTH_INDEFINITE)
                .setAction("Debug") {
                    val myIntent = Intent(this, MainActivity::class.java)
                    startActivity(myIntent)
                }.show()
        }
    }

    override fun onBackPressed() {
        finishAndRemoveTask()
    }

    private fun setupDevicesList() {
        val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val btAdaper = btManager.adapter
        if (!btAdaper.isEnabled) {
            Toast.makeText(this, R.string.message_bluetooth_disabled, Toast.LENGTH_SHORT).show()
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val lastMac = prefs.getString(PREF_LAST_MAC, null)
        pairedDevices = btAdaper.bondedDevices.sortedBy { it.address != lastMac }.toSet()

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
                    Snackbar.make(layout, R.string.message_bluetooth_denied, Snackbar.LENGTH_INDEFINITE)
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

    private fun startCommandStation(connection: BluetoothConnection, deviceName: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val powerOn = prefs.getBoolean("power_at_start", false)
        val locoSortOrder = prefs.getString("sort_locos", LocomotivesStore.SORT_UNSORTED)
        val accSortOrder = prefs.getString("sort_accessories", AccessoriesStore.SORT_UNSORTED)
        val routeSortOrder = prefs.getString("sort_routes", RoutesStore.SORT_UNSORTED)

        try {
            loadStoreFromFile(LocomotivesStore, locoSortOrder)
        }
        catch (e: Exception) {
            Toast.makeText(this, R.string.message_failed_load_locos, Toast.LENGTH_SHORT).show()
        }
        try {
            loadStoreFromFile(AccessoriesStore, accSortOrder)
        }
        catch (e: Exception) {
            Toast.makeText(this, R.string.message_failed_load_acc, Toast.LENGTH_SHORT).show()
        }
        try {
            loadStoreFromFile(RoutesStore, routeSortOrder)
        }
        catch (e: Exception) {
            Toast.makeText(this, R.string.message_failed_load_routes, Toast.LENGTH_SHORT).show()
        }

        CommandStation.setConnection(connection, deviceName)
        LocomotivesStore.data.value?.filter { it.slot > 0 }?.sortedBy { it.slot }?.forEach {
            // assign loco
            CommandStation.stopLocomotive(it.slot)
        }
        CommandStation.setTrackPower(powerOn)
    }

    private fun loadStoreFromFile(store: JsonStoreInterface, sortOrder: String?) {
        val fileName = "${store.javaClass.simpleName}.json"
        val file = File(filesDir, fileName)
        if (file.exists()) {
            val bufferedReader = file.bufferedReader()
            val jsonString = bufferedReader.use {
                it.readText()
            }
            val jsonArray = JSONArray(jsonString)
            store.fromJson(jsonArray, sortOrder)
        }
    }

    companion object {
        const val PREF_LAST_MAC = "last_mac"
        const val PREF_CONNECT = "connect_at_start"
    }
}