package ru.aleksandr.dccppthrottle

import android.content.Intent
import android.os.SystemClock
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.Button
import android.view.View
import android.widget.ScrollView
import androidx.viewpager2.widget.ViewPager2
import ru.aleksandr.dccppthrottle.ui.prog.ProgFragment
import ru.aleksandr.dccppthrottle.ui.prog.SpeedCurveFragment
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2MappingViewModel
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp4SettingsViewModel
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp5SettingsViewModel
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp4SimpleMappingViewModel
import ru.aleksandr.dccppthrottle.ui.decoder.piko.Xp5SimpleMappingViewModel
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
    private fun settleScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        SystemClock.sleep(700)
        instrumentation.waitForIdleSync()
    }

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
                    R.id.action_lp5_settings, R.id.action_xp4_outputs, R.id.action_sw2_mapping,
                    R.id.action_xp4_settings, R.id.action_xp5_settings,
                    R.id.action_xp4_mapping_simple, R.id.action_xp5_mapping_simple)) {
                    for (landscape in listOf(false, true)) {
                        val intent = Intent(context, DecoderActivity::class.java).putExtra("menuid", menu)
                        ActivityScenario.launch<DecoderActivity>(intent).use { scenario ->
                            scenario.onActivity {
                                it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            settleScreen()
                            scenario.onActivity {
                                val provider = ViewModelProvider(it)
                                when (menu) {
                                    R.id.action_sw2_settings -> provider[Sw2SettingsViewModel::class.java].setLoaded(true)
                                    R.id.action_sw2_outputs -> provider[Sw2OutputsViewModel::class.java].setLoaded(true)
                                    R.id.action_lp5_settings -> provider[Lp5SettingsViewModel::class.java].setLoaded(true)
                                    R.id.action_sw2_mapping -> provider[Sw2MappingViewModel::class.java].setLoaded(true)
                                    R.id.action_xp4_settings -> provider[Xp4SettingsViewModel::class.java].setLoaded(true)
                                    R.id.action_xp5_settings -> provider[Xp5SettingsViewModel::class.java].setLoaded(true)
                                    R.id.action_xp4_mapping_simple -> provider[Xp4SimpleMappingViewModel::class.java].setLoaded(true)
                                    R.id.action_xp5_mapping_simple -> provider[Xp5SimpleMappingViewModel::class.java].setLoaded(true)
                                    R.id.action_xp4_outputs -> provider[Xp4OutputsViewModel::class.java].setLoaded(true)
                                }
                            }
                            settleScreen()
                            val name = "${theme}_${context.resources.getResourceEntryName(menu)}_${if (landscape) "land" else "port"}"
                            scenario.onActivity {
                                assertEquals(if (theme == "dark") Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO,
                                    it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                                assertEquals("$name: orientation", if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT,
                                    it.resources.configuration.orientation)
                                assertTrue("$name: activity window focused", it.hasWindowFocus())
                                val button = it.findViewById<Button>(R.id.buttonWrite)
                                assertEquals("$name: no extra bottom margin", (button.parent as View).height, button.bottom)
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

    @Test
    fun programmingWriteButtonsFitAfterScrollingInBothThemesAndOrientations() {
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
                for (landscape in listOf(false, true)) {
                    ActivityScenario.launch(ProgActivity::class.java).use { scenario ->
                        scenario.onActivity {
                            it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                        settleScreen()
                        for (tab in 0..2) {
                            scenario.onActivity { it.findViewById<ViewPager2>(R.id.progPager).setCurrentItem(tab, false) }
                            settleScreen()
                            scenario.onActivity { activity ->
                                if (tab > 0) {
                                    val fragment = activity.supportFragmentManager.fragments.filterIsInstance<SpeedCurveFragment>()
                                        .first { it.arguments?.getBoolean("extended") == (tab == 2) }
                                    val root = fragment.requireView()
                                    val scroll = if (root is ScrollView) root else root.findViewById<ScrollView>(R.id.curveControlsScroll)
                                    scroll.isSmoothScrollingEnabled = false
                                    scroll.fullScroll(View.FOCUS_DOWN)
                                }
                            }
                            settleScreen()
                            val name = "${theme}_programming_tab${tab}_${if (landscape) "land" else "port"}"
                            scenario.onActivity { activity ->
                                assertEquals("$name: orientation", if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT,
                                    activity.resources.configuration.orientation)
                                assertTrue("$name: activity window focused", activity.hasWindowFocus())
                                val root = if (tab == 0) {
                                    activity.supportFragmentManager.fragments.filterIsInstance<ProgFragment>().first().requireView()
                                } else {
                                    activity.supportFragmentManager.fragments.filterIsInstance<SpeedCurveFragment>()
                                        .first { it.arguments?.getBoolean("extended") == (tab == 2) }.requireView()
                                }
                                val button = root.findViewById<Button>(if (tab == 0) R.id.buttonWrite else R.id.curveWrite)
                                val visible = Rect()
                                assertTrue("$name: button visible", button.getGlobalVisibleRect(visible))
                                assertEquals("$name: full height", button.height, visible.height())
                                assertEquals("$name: full width", button.width, visible.width())
                                assertTrue("$name: label fits", button.layout.height <=
                                    button.height - button.compoundPaddingTop - button.compoundPaddingBottom)
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
