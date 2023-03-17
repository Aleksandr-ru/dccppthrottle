/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

open class AwakeActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()

        val key = getString(R.string.pref_key_screen_on)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val screenOn = prefs.getBoolean(key, false)
        if (screenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}