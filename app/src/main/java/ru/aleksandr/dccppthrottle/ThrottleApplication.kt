package ru.aleksandr.dccppthrottle

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class ThrottleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePreference.apply(this)
    }
}

object ThemePreference {
    fun apply(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val mode = when (preferences.getString(context.getString(R.string.pref_key_theme), "system")) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
