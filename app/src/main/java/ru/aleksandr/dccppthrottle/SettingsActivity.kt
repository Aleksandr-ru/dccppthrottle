/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.json.JSONArray
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.store.*
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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

    // https://stackoverflow.com/a/68883413
    class CreateMimeDocument(private val mimeType: String) : CreateDocument() {
        override fun createIntent(context: Context, input: String): Intent {
            return super.createIntent(context, input).setType(mimeType)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        private val TAG = javaClass.simpleName
        private val mimeTypeZip = "application/zip"
        private val storeList = listOf(LocomotivesStore, AccessoriesStore, RoutesStore)

        private lateinit var backupPref: Preference
        private lateinit var restorePref: Preference

        private val backupLauncher = registerForActivityResult(
            CreateMimeDocument(mimeTypeZip)
        ) { uri ->
            try {
                backupPref.isEnabled = false
                restorePref.isEnabled = false
                backupToFile(uri)
                Toast.makeText(this.context, R.string.message_backup_ok, Toast.LENGTH_SHORT).show()
                try {
                    // https://stackoverflow.com/a/25005243
                    val filename = context?.contentResolver?.query(uri, null, null, null, null)?.use {
                        it.moveToFirst()
                        it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    } ?: throw Exception("Failed to get filename from uri")
                    backupPref.summary = getString(R.string.label_backup_complete, filename)
                }
                catch (e: Exception) {
                    backupPref.summary = getString(R.string.label_backup_complete, uri.path)
                }
            }
            catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                Toast.makeText(this.context, R.string.message_backup_error, Toast.LENGTH_SHORT).show()
                backupPref.summary = getString(R.string.label_backup_error, e.message)
            }
            finally {
                backupPref.isEnabled = true
                restorePref.isEnabled = true
            }
        }

        private val restoreLauncher = registerForActivityResult(OpenDocument()) { uri ->
            try {
                backupPref.isEnabled = false
                restorePref.isEnabled = false
                CommandStation.unassignAll()
                restoreFromFile(uri)
                Toast.makeText(this.context, R.string.message_restore_ok, Toast.LENGTH_SHORT).show()
                restorePref.summary = getString(R.string.label_restore_complete)
            }
            catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                Toast.makeText(this.context, R.string.message_restore_error, Toast.LENGTH_SHORT).show()
                restorePref.summary = getString(R.string.label_restore_error, e.message)
            }
            finally {
                backupPref.isEnabled = true
                restorePref.isEnabled = true
            }
        }

        // https://stackoverflow.com/a/72451941
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            backupPref = findPreference<Preference>(getString(R.string.pref_key_backup))!!
            backupPref.setOnPreferenceClickListener {
                val suffix = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                }
                else "backup"
                val filename = getString(R.string.filename_backup, suffix)
                backupLauncher.launch(filename)
                true
            }

            restorePref = findPreference<Preference>(getString(R.string.pref_key_restore))!!
            restorePref.setOnPreferenceClickListener {
                restoreLauncher.launch(arrayOf(mimeTypeZip))
                true
            }
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
            val keySpeedSteps = getString(R.string.pref_key_speed_steps)
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
                keySpeedSteps -> {
                    val keyVal = pref.getString(key, null)
                    keyVal?.let {
                        CommandStation.emergencyStop()
                        CommandStation.setSpeedSteps(it)
                    }
                }
                else -> {}
            }
        }

        // https://gist.github.com/kairos34/75f782b029540e60c2f3b69e5166588e
        private fun backupToFile(uri: Uri) {
            val storeFiles: List<File> = storeList.map {
                val filename = getString(R.string.filename_store, it.javaClass.simpleName)
                File(this.context!!.filesDir, filename)
            }.filter { it.isFile }

            val contentResolver = this.context!!.contentResolver
            val outputStream = contentResolver.openOutputStream(uri)
            ZipOutputStream(outputStream).use { output ->
                storeFiles.forEach { file ->
                    FileInputStream(file).use { input ->
                        val entry = ZipEntry(file.name)
                        output.putNextEntry(entry)
                        input.copyTo(output)
                    }
                    if (BuildConfig.DEBUG) Log.i(TAG, "Compressed: " + file.name)
                }
            }
        }

        // https://stackoverflow.com/a/66683493
        private fun restoreFromFile(uri: Uri) {
            val storeFiles: Map<String, JsonStoreInterface> = storeList.map {
                getString(R.string.filename_store, it.javaClass.simpleName) to it
            }.toMap()

            val contentResolver = this.context!!.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            var restoredLength = 0
            ZipInputStream(inputStream).use { input ->
                generateSequence { input.nextEntry }.filter { storeFiles.containsKey(it.name) }.forEach { entry ->
                    val jsonString = String(input.readBytes())
                    val jsonArray = JSONArray(jsonString)
                    storeFiles[entry.name]?.apply {
                        fromJson(jsonArray)
                        hasUnsavedData = true
                    }
                    if (BuildConfig.DEBUG) Log.i(TAG, "Restored: " + entry.name)
                    restoredLength += jsonString.length
                }
            }
            if (restoredLength == 0) {
                throw Exception("Nothing restored")
            }
        }
    }
}