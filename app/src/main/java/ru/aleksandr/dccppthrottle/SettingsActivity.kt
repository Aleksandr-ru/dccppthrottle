package ru.aleksandr.dccppthrottle

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.RoutesStore

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        // https://stackoverflow.com/a/72451941
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onResume() {
            super.onResume()
            val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)
            prefs.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)
            prefs.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(pref: SharedPreferences?, key: String?) {
            if (pref != null && key != null) when(key) {
                "sort_locos" -> {
                    val keyVal = pref.getString(key, LocomotivesStore.SORT_UNSORTED)
                    LocomotivesStore.setSortOrder(keyVal!!)
                }
                "sort_accessories" -> {
                    val keyVal = pref.getString(key, AccessoriesStore.SORT_UNSORTED)
                    AccessoriesStore.setSortOrder(keyVal!!)
                }
                "sort_routes" -> {
                    val keyVal = pref.getString(key, RoutesStore.SORT_UNSORTED)
                    RoutesStore.setSortOrder(keyVal!!)
                }
                else -> {}
            }
        }
    }
}