/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.prog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_console -> {
                val myIntent = Intent(this, ConsoleActivity::class.java)
                startActivity(myIntent)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}