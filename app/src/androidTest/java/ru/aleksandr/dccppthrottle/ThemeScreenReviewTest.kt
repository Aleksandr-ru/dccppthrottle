package ru.aleksandr.dccppthrottle

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.lifecycle.ViewModelProvider
import ru.aleksandr.dccppthrottle.ui.decoder.CvListModel
import ru.aleksandr.dccppthrottle.ui.decoder.esu.*
import ru.aleksandr.dccppthrottle.ui.decoder.piko.*
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.*
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.dialogs.PomValueDialog
import ru.aleksandr.dccppthrottle.store.*
import java.io.File

/** Opt in with -e screenshots true. PNGs are written to external files/theme-review. */
@RunWith(AndroidJUnit4::class)
class ThemeScreenReviewTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val stores = listOf(LocomotivesStore, AccessoriesStore, RoutesStore)

    private fun capture(name: String) {
        instrumentation.waitForIdleSync()
        SystemClock.sleep(700)
        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        val file = File(context.getExternalFilesDir("theme-review"), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun screen(name: String, type: Class<out AppCompatActivity>,
                       extra: Pair<String, Int>? = null, landscape: Boolean = false,
                       prepare: (AppCompatActivity) -> Unit = {}) {
        val filter = InstrumentationRegistry.getArguments().getString("screen")
        if (filter != null && !name.contains(filter)) return
        val intent = Intent(context, type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        extra?.let { intent.putExtra(it.first, it.second) }
        ActivityScenario.launch<AppCompatActivity>(intent).use { scenario ->
            scenario.onActivity {
                it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                if (type == DecoderActivity::class.java) {
                    val provider = ViewModelProvider(it)
                    when (extra?.second) {
                        R.id.action_lp5_mapping -> provider[Lp5MappingViewModel::class.java].setLoaded(true)
                        R.id.action_lp5_outputs -> provider[Lp5OutputsViewModel::class.java].setLoaded(true)
                        R.id.action_xp5_outputs -> provider[Xp5OutputsViewModel::class.java].setLoaded(true)
                        R.id.action_xp4_mapping -> provider[Xp4MappingViewModel::class.java].setLoaded(true)
                        else -> {
                            val model: CvListModel = when (extra?.second) {
                                R.id.action_lp5_settings -> provider[Lp5SettingsViewModel::class.java]
                                R.id.action_sw2_mapping -> provider[Sw2MappingViewModel::class.java]
                                R.id.action_sw2_outputs -> provider[Sw2OutputsViewModel::class.java]
                                R.id.action_sw2_settings -> provider[Sw2SettingsViewModel::class.java]
                                R.id.action_xp4_mapping_simple -> provider[Xp4SimpleMappingViewModel::class.java]
                                R.id.action_xp4_outputs -> provider[Xp4OutputsViewModel::class.java]
                                R.id.action_xp4_settings -> provider[Xp4SettingsViewModel::class.java]
                                R.id.action_xp5_mapping_simple -> provider[Xp5SimpleMappingViewModel::class.java]
                                else -> provider[Xp5SettingsViewModel::class.java]
                            }
                            model.setLoaded(true)
                        }
                    }
                }
                prepare(it)
            }
            capture(name)
        }
        instrumentation.runOnMainSync { stores.forEach { it.hasUnsavedData = false } }
    }

    @Test
    fun captureScreensInBothThemes() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("screenshots") == "true")
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = context.getString(R.string.pref_key_theme)
        val previous = preferences.getString(key, null)
        val saved = mutableListOf<JSONArray>()
        instrumentation.runOnMainSync { stores.forEach { saved.add(it.toJson()) } }
        try {
            instrumentation.runOnMainSync {
                val locos = JSONArray()
                for (slot in 1..2) {
                    locos.put(LocomotivesStore.LocomotiveState(slot + 2, "Test locomotive $slot").apply {
                        this.slot = slot
                        funcNames[0] = "Lights"
                    }.toJson())
                }
                LocomotivesStore.fromJson(locos)
                AccessoriesStore.fromJson(JSONArray().put(AccessoriesStore.AccessoryState(1, "Turnout").toJson()))
                RoutesStore.fromJson(JSONArray().put(RoutesStore.RouteState("Test route").apply {
                    accessories.add(RoutesStore.RouteStateAccessory(1))
                }.toJson()))
            }
            instrumentation.waitForIdleSync()
            val speed = InstrumentationRegistry.getArguments().getString("speed")
                ?.toIntOrNull()?.coerceIn(0, 100) ?: 0
            instrumentation.runOnMainSync {
                LocomotivesStore.setSpeedBySlot(1, speed, false)
                LocomotivesStore.setSpeedBySlot(2, speed, false)
            }
            instrumentation.waitForIdleSync()
            for (theme in listOf("light", "dark")) {
                instrumentation.runOnMainSync {
                    preferences.edit().putString(key, theme).commit()
                    ThemePreference.apply(context)
                }
                val mode = if (theme == "dark") Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                screen("${theme}_settings", SettingsActivity::class.java) {
                    assertEquals(mode, it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                }
                for ((name, page) in listOf("locomotives" to 0, "accessories" to 1, "routes" to 2)) {
                    instrumentation.runOnMainSync { MainStore.setMainViewPagerPosition(page) }
                    screen("${theme}_$name", MainActivity::class.java)
                }
                screen("${theme}_drawer", MainActivity::class.java) {
                    it.findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(GravityCompat.START, false)
                }
                screen("${theme}_overflow", MainActivity::class.java) { it.openOptionsMenu() }
                for (landscape in listOf(false, true)) {
                    val suffix = if (landscape) "land" else "port"
                    screen("${theme}_cab_$suffix", LocoCabActivity::class.java, landscape = landscape)
                    screen("${theme}_dual_$suffix", DualCabActivity::class.java, landscape = landscape)
                    screen("${theme}_prog_$suffix", ProgActivity::class.java, landscape = landscape)
                }
                screen("${theme}_loco_dialog", LocoCabActivity::class.java) {
                    LocomotiveDialog.storeIndex = 0
                    LocomotiveDialog().showNow(it.supportFragmentManager, LocomotiveDialog.TAG)
                }
                screen("${theme}_pom_dialog", LocoCabActivity::class.java) {
                    PomValueDialog.cv = 1
                    PomValueDialog().showNow(it.supportFragmentManager, PomValueDialog.TAG)
                }
                screen("${theme}_functions", FunctionsActivity::class.java, FunctionsActivity.ARG_SLOT to 1)
                screen("${theme}_route_editor", RouteEditorActivity::class.java)
                instrumentation.runOnMainSync {
                    ConsoleStore.clear()
                    ConsoleStore.addIn("<iDCC-EX test station>")
                    ConsoleStore.addOut("<s>")
                }
                screen("${theme}_console", ConsoleActivity::class.java)
                for (menu in listOf(
                    R.id.action_lp5_mapping, R.id.action_lp5_outputs, R.id.action_lp5_settings,
                    R.id.action_sw2_mapping, R.id.action_sw2_outputs, R.id.action_sw2_settings,
                    R.id.action_xp4_mapping_simple, R.id.action_xp4_mapping, R.id.action_xp4_outputs,
                    R.id.action_xp4_settings, R.id.action_xp5_mapping_simple, R.id.action_xp5_outputs,
                    R.id.action_xp5_settings
                )) {
                    screen("${theme}_${context.resources.getResourceEntryName(menu)}", DecoderActivity::class.java, "menuid" to menu)
                }
                screen("${theme}_connect", ConnectActivity::class.java)
            }
        } finally {
            instrumentation.runOnMainSync {
                stores.forEachIndexed { index, store -> store.fromJson(saved[index]) }
                if (previous == null) preferences.edit().remove(key).commit()
                else preferences.edit().putString(key, previous).commit()
                ThemePreference.apply(context)
            }
        }
    }
}
