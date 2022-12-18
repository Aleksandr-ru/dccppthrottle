package ru.aleksandr.dccppthrottle

//import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.ceil


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val MAX_BUTTONS = 10
        val BUTTONS_PER_ROW = 4
        val rows = ceil(MAX_BUTTONS.toDouble() / BUTTONS_PER_ROW.toDouble()).toInt()
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        var i = 0
        for (r in 0 until rows) {
            var tableRow = TableRow(this)
            for (b in 0 until BUTTONS_PER_ROW) {
                var button = ToggleButton(this)
                button.text = "F$i"
                tableRow.addView(button, b)
                i++
                if (i >= MAX_BUTTONS) break
            }
            tableLayout.addView(tableRow, r)
        }

        val btn = findViewById<Button>(R.id.button1);
        btn.setOnClickListener {
            val myIntent = Intent(this, MenuActivity::class.java)
            startActivity(myIntent)
        }
    }
}