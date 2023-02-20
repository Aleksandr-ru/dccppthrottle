package ru.aleksandr.dccppthrottle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.aleksandr.dccppthrottle.ui.prog.ProgFragment

class ProgActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prog)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProgFragment.newInstance())
                .commitNow()
        }
    }
}