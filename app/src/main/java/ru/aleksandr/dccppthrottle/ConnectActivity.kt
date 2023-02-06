package ru.aleksandr.dccppthrottle

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.aleksandr.dccppthrottle.store.MockStore

class ConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val ver = findViewById<TextView>(R.id.textViewVersion)
        ver.text = String.format(getString(R.string.app_version), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        val spinner: Spinner = findViewById(R.id.spinnerBtList)
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, MockStore.ramdomBluetoothList())
        spinner.adapter = adapter

        val btn = findViewById<Button>(R.id.btnConnect)
        btn.setOnClickListener {
            val myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)
        }
    }
}