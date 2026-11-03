package ru.aleksandr.dccppthrottle

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.aleksandr.dccppthrottle.ui.decoder.esu.Lp5SettingsViewModel
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2OutputsViewModel
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2SettingsViewModel
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp4OutputsViewModel
import java.io.File

@RunWith(AndroidJUnit4::class)
class DecoderLayoutTest {
    @Test
    fun writeButtonAndLabelFitOnScreenInBothThemesAndOrientations() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = context.getString(R.string.pref_key_theme)
        val previous = preferences.getString(key, null)
        try {
            for (theme in listOf("dark", "light")) {
                instrumentation.runOnMainSync {
                    preferences.edit().putString(key, theme).commit()
                    ThemePreference.apply(context)
                }
                for (menu in listOf(R.id.action_sw2_settings, R.id.action_sw2_outputs,
                    R.id.action_lp5_settings, R.id.action_xp4_outputs)) {
                    for (landscape in listOf(false, true)) {
                        val intent = Intent(context, DecoderActivity::class.java).putExtra("menuid", menu)
                        ActivityScenario.launch<DecoderActivity>(intent).use { scenario ->
                            scenario.onActivity {
                                it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            instrumentation.waitForIdleSync()
                            scenario.onActivity {
                                val provider = ViewModelProvider(it)
                                when (menu) {
                                    R.id.action_sw2_settings -> provider[Sw2SettingsViewModel::class.java].setLoaded(true)
                                    R.id.action_sw2_outputs -> provider[Sw2OutputsViewModel::class.java].setLoaded(true)
                                    R.id.action_lp5_settings -> provider[Lp5SettingsViewModel::class.java].setLoaded(true)
                                    R.id.action_xp4_outputs -> provider[Xp4OutputsViewModel::class.java].setLoaded(true)
                                }
                            }
                            instrumentation.waitForIdleSync()
                            val name = "${theme}_${context.resources.getResourceEntryName(menu)}_${if (landscape) "land" else "port"}"
                            scenario.onActivity {
                                assertEquals(if (theme == "dark") Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO,
                                    it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                                val button = it.findViewById<Button>(R.id.buttonWrite)
                                val visible = Rect()
                                assertTrue("$name: button visible", button.getGlobalVisibleRect(visible))
                                assertEquals("$name: full button height", button.height, visible.height())
                                assertTrue("$name: touch target height", visible.height() >= 48 * it.resources.displayMetrics.density)
                                assertEquals(it.getString(R.string.action_write_changed), button.text.toString())
                                val textHeight = button.layout.height
                                assertTrue("$name: label fits", textHeight <= button.height - button.compoundPaddingTop - button.compoundPaddingBottom)
                            }
                            val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                            File(context.getExternalFilesDir("decoder-layout"), "$name.png")
                                .outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                            bitmap.recycle()
                        }
                    }
                }
            }
        } finally {
            instrumentation.runOnMainSync {
                if (previous == null) preferences.edit().remove(key).commit()
                else preferences.edit().putString(key, previous).commit()
                ThemePreference.apply(context)
            }
        }
    }
}
