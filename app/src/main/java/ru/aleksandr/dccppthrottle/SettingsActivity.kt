package ru.aleksandr.dccppthrottle

import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.RoutesStore

class SettingsActivity : AwakeActivity() {

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
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(pref: SharedPreferences?, key: String?) {
            val keyScreenOn = getString(R.string.pref_key_screen_on)
            val keySortLocos = getString(R.string.pref_key_sort_locos)
            val keySortAcc = getString(R.string.pref_key_sort_acc)
            val keySortRoutes = getString(R.string.pref_key_sort_routes)
            if (pref != null && key != null) when(key) {
                keyScreenOn -> {
                    val screenOn = pref.getBoolean(key, false)
                    val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    activity?.window?.apply {
                        if (screenOn) addFlags(flag)
                        else clearFlags(flag)
                    }
                }
                keySortLocos -> {
                    val keyVal = pref.getString(key, LocomotivesStore.SORT_UNSORTED)
                    LocomotivesStore.setSortOrder(keyVal!!)
                }
                keySortAcc -> {
                    val keyVal = pref.getString(key, AccessoriesStore.SORT_UNSORTED)
                    AccessoriesStore.setSortOrder(keyVal!!)
                }
                keySortRoutes -> {
                    val keyVal = pref.getString(key, RoutesStore.SORT_UNSORTED)
                    RoutesStore.setSortOrder(keyVal!!)
                }
                else -> {}
            }
        }
    }
}