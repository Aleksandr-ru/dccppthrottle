package ru.aleksandr.dccppthrottle

import android.os.Bundle
import ru.aleksandr.dccppthrottle.ui.prog.ProgFragment

class ProgActivity : AwakeActivity() {

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