package ru.aleksandr.dccppthrottle

import android.content.res.Configuration
import android.os.Parcelable
import android.util.SparseArray
import android.view.ContextThemeWrapper
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import ru.aleksandr.dccppthrottle.view.PlusMinusView

@RunWith(AndroidJUnit4::class)
class ThemeRecreationTest {
    @Test
    fun consoleMessagesHaveReadableContrastInBothThemes() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = context.getString(R.string.pref_key_theme)
        val previous = preferences.getString(key, null)
        try {
            for (theme in listOf("light", "dark")) {
                instrumentation.runOnMainSync {
                    preferences.edit().putString(key, theme).commit()
                    ThemePreference.apply(context)
                    ru.aleksandr.dccppthrottle.store.ConsoleStore.clear()
                    ru.aleksandr.dccppthrottle.store.ConsoleStore.addIn("Incoming test message")
                    ru.aleksandr.dccppthrottle.store.ConsoleStore.addOut("Outgoing test message")
                }
                ActivityScenario.launch(ConsoleActivity::class.java).use { scenario ->
                    instrumentation.waitForIdleSync()
                    scenario.onActivity { activity ->
                        val list = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.listConsole)
                        assertEquals(2, list.childCount)
                        val background = androidx.core.content.ContextCompat.getColor(activity, R.color.app_background)
                        for (index in 0 until list.childCount) {
                            val text = list.getChildAt(index) as android.widget.TextView
                            val contrast = androidx.core.graphics.ColorUtils.calculateContrast(text.currentTextColor, background)
                            org.junit.Assert.assertTrue("$theme console contrast: $contrast", contrast >= 4.5)
                        }
                    }
                }
            }
        } finally {
            instrumentation.runOnMainSync {
                if (previous == null) preferences.edit().remove(key).commit()
                else preferences.edit().putString(key, previous).commit()
                ThemePreference.apply(context)
                ru.aleksandr.dccppthrottle.store.ConsoleStore.clear()
            }
        }
    }

    @Test
    fun decoderTitleSurvivesRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = android.content.Intent(context, DecoderActivity::class.java)
            .putExtra("menuid", R.id.action_lp5_outputs)
        ActivityScenario.launch<DecoderActivity>(intent).use { scenario ->
            var title = ""
            scenario.onActivity { title = it.title.toString() }
            scenario.recreate()
            scenario.onActivity { assertEquals(title, it.title.toString()) }
        }
    }

    @Test
    fun independentNumericInputsSurviveHierarchyRestorationInBothThemes() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            for (nightMode in listOf(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES)) {
                val context = instrumentation.targetContext
                val configuration = Configuration(context.resources.configuration).apply {
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
                }
                val themed = ContextThemeWrapper(
                    context.createConfigurationContext(configuration), R.style.Theme_DCCppThrottle
                )
                fun inputs(): LinearLayout = LinearLayout(themed).apply {
                    addView(PlusMinusView(themed).apply { id = R.id.plusminusCvNum })
                    addView(PlusMinusView(themed).apply { id = R.id.plusminusCvValue; nullable = true })
                }
                val original = inputs()
                (original.getChildAt(0) as PlusMinusView).value = 123
                (original.getChildAt(1) as PlusMinusView).value = null
                val state = SparseArray<Parcelable>()
                original.saveHierarchyState(state)
                val restored = inputs()
                restored.restoreHierarchyState(state)
                assertEquals(123, (restored.getChildAt(0) as PlusMinusView).value)
                val nullable = restored.getChildAt(1) as PlusMinusView
                assertEquals(null, nullable.value)
                nullable.isEnabled = false
                assertFalse(nullable.getChildAt(0).isEnabled)
            }
        }
    }

    @Test
    fun selectedThemeAppliesToSettingsAndSurvivesRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = context.getString(R.string.pref_key_theme)
        val previous = preferences.getString(key, null)
        try {
            for ((value, mode) in listOf(
                "light" to Configuration.UI_MODE_NIGHT_NO,
                "dark" to Configuration.UI_MODE_NIGHT_YES
            )) {
                instrumentation.runOnMainSync {
                    preferences.edit().putString(key, value).commit()
                    ThemePreference.apply(context)
                }
                ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
                    scenario.recreate()
                    scenario.onActivity { activity ->
                        assertEquals(mode, activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                        assertEquals(value, preferences.getString(key, null))
                    }
                }
            }
            instrumentation.runOnMainSync {
                preferences.edit().putString(key, "system").commit()
                ThemePreference.apply(context)
                assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.getDefaultNightMode())
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
