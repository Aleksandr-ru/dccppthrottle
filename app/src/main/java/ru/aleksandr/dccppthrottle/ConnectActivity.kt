package ru.aleksandr.dccppthrottle

//import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class ConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val item = "My Bluetooth device 1"
        val list = ArrayList<String>()
        list.add(item)
        list.add(item.reversed())
        val spinner: Spinner = findViewById(R.id.spinnerBtList)
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
        spinner.adapter = adapter
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.planets_array,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            // Specify the layout to use when the list of choices appears
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner
//            spinner.adapter = adapter
//        }
//
//        val MAX_BUTTONS = 10
//        val BUTTONS_PER_ROW = 4
//        val rows = ceil(MAX_BUTTONS.toDouble() / BUTTONS_PER_ROW.toDouble()).toInt()
//        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
//        var i = 0
//        for (r in 0 until rows) {
//            var tableRow = TableRow(this)
//            for (b in 0 until BUTTONS_PER_ROW) {
//                var button = ToggleButton(this)
//                button.text = "F$i"
//                tableRow.addView(button, b)
//                i++
//                if (i >= MAX_BUTTONS) break
//            }
//            tableLayout.addView(tableRow, r)
//        }

        val btn = findViewById<Button>(R.id.btnConnect);
        btn.setOnClickListener {
            val myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)
        }
    }
}