package ru.aleksandr.dccppthrottle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.color.MaterialColors
import ru.aleksandr.dccppthrottle.store.ConsoleStore

class ConsoleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_console)

        val scrollView = findViewById<ScrollView>(R.id.scrollViewConsole)
        val listView = findViewById<LinearLayout>(R.id.listConsole)
        val linearLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        ConsoleStore.data.observe(this) {
            it.forEach {
                val drawableId =
                    if (it.tag == ConsoleStore.TAG_IN) R.drawable.ic_arrow_south_west_24
                    else R.drawable.ic_arrow_north_east_24
                listView.addView(TextView(this).apply {
                    layoutParams = linearLayoutParams
                    breakStrategy = Layout.BREAK_STRATEGY_SIMPLE
                    gravity = Gravity.CENTER_VERTICAL
                    text = it.str

                    val color = MaterialColors.getColor(this,
                        if (it.tag == ConsoleStore.TAG_OUT) com.google.android.material.R.attr.colorPrimaryVariant
                        else com.google.android.material.R.attr.colorSecondaryVariant
                    )
                    setTextColor(color)
                    setPadding(0,8,8,8)
                    setHorizontallyScrolling(false)
                    setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0)
                })
            }
            scrollView.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }
}