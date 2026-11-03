package ru.aleksandr.dccppthrottle

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.store.MockStore
import ru.aleksandr.dccppthrottle.ui.prog.SpeedCurveFragment
import ru.aleksandr.dccppthrottle.ui.prog.SpeedCurveViewModel
import ru.aleksandr.dccppthrottle.view.AdjustableCurveView
import ru.aleksandr.dccppthrottle.view.LockableScrollView
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProgrammingCurvesTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun select(scenario: ActivityScenario<ProgActivity>, tab: Int) {
        scenario.onActivity { it.findViewById<ViewPager2>(R.id.progPager).setCurrentItem(tab, false) }
        instrumentation.waitForIdleSync()
    }

    private fun screen(activity: ProgActivity, extended: Boolean): View =
        activity.supportFragmentManager.fragments.filterIsInstance<SpeedCurveFragment>()
            .first { it.arguments?.getBoolean("extended") == extended }.requireView()

    private fun input(view: View, cv: Int): PlusMinusView {
        val containers = listOf(R.id.curveRows, R.id.curveTrimRows)
            .map { view.findViewById<LinearLayout>(it) }
        return containers.flatMap { rows ->
            (0 until rows.childCount).map { rows.getChildAt(it).findViewById<PlusMinusView>(R.id.curveCvInput) }
        }.first { it.tag == cv }
    }

    private fun waitForTransfer(scenario: ActivityScenario<ProgActivity>) {
        val until = SystemClock.uptimeMillis() + 12000
        while (SystemClock.uptimeMillis() < until) {
            var busy = true
            scenario.onActivity { busy = ViewModelProvider(it)[SpeedCurveViewModel::class.java].busy.value == true }
            if (!busy) { instrumentation.waitForIdleSync(); return }
            SystemClock.sleep(50)
        }
        fail("CV operation did not finish")
    }

    private fun touch(graph: AdjustableCurveView, action: Int, x: Float, y: Float) {
        val event = MotionEvent.obtain(0, 0, action, x, y, 0)
        try { assertTrue(graph.onTouchEvent(event)) } finally { event.recycle() }
    }

    @Test
    fun defaultTabAndBidirectionalEditingSurviveRecreation() {
        ActivityScenario.launch(ProgActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertEquals(0, activity.findViewById<ViewPager2>(R.id.progPager).currentItem)
                assertEquals(listOf("CV", "Speed Curve", "Extended Curve"),
                    activity.findViewById<TabLayout>(R.id.progTabs).let { tabs ->
                        (0..2).map { tabs.getTabAt(it)?.text.toString() }
                    })
            }
            select(scenario, 1)
            scenario.onActivity { activity ->
                val view = screen(activity, false)
                val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                assertEquals(3, graph.points.size)
                assertEquals(0, graph.smoothing)
                input(view, 6).value = 80
                assertEquals(80, graph.points[1])
                val middleX = graph.width / 2f
                val inset = 8f * graph.resources.displayMetrics.density
                val pointY = graph.height - inset - 80f / 255 * (graph.height - 2 * inset)
                touch(graph, MotionEvent.ACTION_DOWN, middleX, pointY)
                assertFalse((view as LockableScrollView).isScrollable())
                assertFalse(activity.findViewById<ViewPager2>(R.id.progPager).isUserInputEnabled)
                touch(graph, MotionEvent.ACTION_MOVE, middleX, -1000f)
                // A model update must not finish the active drag.
                assertFalse(view.isScrollable())
                assertEquals(255, input(view, 6).value)
                touch(graph, MotionEvent.ACTION_UP, middleX, -1000f)
                assertTrue(view.isScrollable())
                assertTrue(activity.findViewById<ViewPager2>(R.id.progPager).isUserInputEnabled)
            }
            scenario.recreate()
            instrumentation.waitForIdleSync()
            scenario.onActivity { activity ->
                assertEquals(1, activity.findViewById<ViewPager2>(R.id.progPager).currentItem)
                assertEquals(255, screen(activity, false).findViewById<AdjustableCurveView>(R.id.curveGraph).points[1])
            }
        }
    }

    @Test
    fun mockReadWriteIncludesAllValuesAndPreservesOtherCv29Bits() {
        assertFalse(CommandStation.isConnected())
        ActivityScenario.launch(ProgActivity::class.java).use { scenario ->
            select(scenario, 1)
            scenario.onActivity { activity ->
                MockStore.setProgrammingCvValue(29, 0xB7)
                val view = screen(activity, false)
                input(view, 2).value = 11
                input(view, 6).value = 122
                input(view, 5).value = 233
                view.findViewById<SwitchMaterial>(R.id.curveMode).isChecked = true
                view.findViewById<Button>(R.id.curveWrite).performClick()
                assertTrue(ViewModelProvider(activity)[SpeedCurveViewModel::class.java].busy.value!!)
            }
            waitForTransfer(scenario)
            scenario.onActivity { activity ->
                assertEquals(0xA7, MockStore.programmingCvValue(29))
                assertEquals(listOf(11, 122, 233), listOf(2, 6, 5).map { MockStore.programmingCvValue(it) })
                val view = screen(activity, false)
                input(view, 6).value = 1
                view.findViewById<Button>(R.id.curveRead).performClick()
            }
            waitForTransfer(scenario)
            scenario.onActivity { assertEquals(122, input(screen(it, false), 6).value) }
            select(scenario, 2)
            scenario.onActivity { activity ->
                val view = screen(activity, true)
                assertEquals(28, view.findViewById<LinearLayout>(R.id.curveRows).childCount)
                val trimRows = view.findViewById<LinearLayout>(R.id.curveTrimRows)
                assertEquals(2, trimRows.childCount)
                assertEquals(View.VISIBLE, view.findViewById<View>(R.id.curveTrimSection).visibility)
                for (i in 0 until trimRows.childCount) {
                    val description = trimRows.getChildAt(i).findViewById<android.widget.TextView>(R.id.curveCvDescription)
                    assertEquals(View.VISIBLE, description.visibility)
                    assertTrue(description.text.isNotBlank())
                }
                val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                assertEquals(28, graph.points.size)
                assertEquals(8, graph.smoothing)
                (67..94).forEach { input(view, it).value = it + 30 }
                input(view, 66).value = 129
                input(view, 95).value = 130
                view.findViewById<SwitchMaterial>(R.id.curveMode).isChecked = true
                view.findViewById<Button>(R.id.curveWrite).performClick()
            }
            waitForTransfer(scenario)
            scenario.onActivity { activity ->
                assertEquals(0xB7, MockStore.programmingCvValue(29))
                (67..94).forEach { assertEquals(it + 30, MockStore.programmingCvValue(it)) }
                assertEquals(129, MockStore.programmingCvValue(66))
                assertEquals(130, MockStore.programmingCvValue(95))
                val view = screen(activity, true)
                input(view, 66).value = 0
                input(view, 67).value = 0
                view.findViewById<Button>(R.id.curveRead).performClick()
            }
            waitForTransfer(scenario)
            scenario.onActivity { activity ->
                val view = screen(activity, true)
                assertEquals(129, input(view, 66).value)
                assertEquals(97, input(view, 67).value)
            }
            select(scenario, 1)
            scenario.onActivity { assertFalse(screen(it, false).findViewById<SwitchMaterial>(R.id.curveMode).isChecked) }
        }
    }

    @Test
    fun smoothingToggleIsIndependentAndSurvivesRecreation() {
        ActivityScenario.launch(ProgActivity::class.java).use { scenario ->
            select(scenario, 1)
            scenario.onActivity {
                assertEquals(View.GONE, screen(it, false).findViewById<SwitchMaterial>(R.id.curveSmoothing).visibility)
            }
            select(scenario, 2)
            fun dragMiddle(view: View) {
                val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                val inset = 8f * graph.resources.displayMetrics.density
                val plotHeight = graph.height - 2 * inset
                val x = inset + 14 * (graph.width - 2 * inset) / 27
                val y = graph.height - inset - 100f / 255 * plotHeight
                touch(graph, MotionEvent.ACTION_DOWN, x, y)
                touch(graph, MotionEvent.ACTION_MOVE, x, y - 60f / 255 * plotHeight)
                touch(graph, MotionEvent.ACTION_UP, x, y - 60f / 255 * plotHeight)
            }
            scenario.onActivity { activity ->
                val view = screen(activity, true)
                val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                val toggle = view.findViewById<SwitchMaterial>(R.id.curveSmoothing)
                assertTrue(toggle.isChecked)
                assertEquals(8, graph.smoothing)
                (67..94).forEach { input(view, it).value = 100 }
                val before = ViewModelProvider(activity)[SpeedCurveViewModel::class.java].values.value
                toggle.performClick()
                assertEquals(0, graph.smoothing)
                assertEquals(before, ViewModelProvider(activity)[SpeedCurveViewModel::class.java].values.value)
                dragMiddle(view)
                assertEquals(List(28) { if (it == 14) 160 else 100 }, graph.points)
            }
            scenario.recreate()
            instrumentation.waitForIdleSync()
            scenario.onActivity { activity ->
                val view = screen(activity, true)
                val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                val toggle = view.findViewById<SwitchMaterial>(R.id.curveSmoothing)
                assertFalse(toggle.isChecked)
                assertEquals(0, graph.smoothing)
                input(view, 81).value = 100
                val before = graph.points
                toggle.performClick()
                assertTrue(toggle.isChecked)
                assertEquals(8, graph.smoothing)
                assertEquals(before, graph.points)
                dragMiddle(view)
                assertEquals(160, graph.points[14])
                assertEquals(159, graph.points[13])
                assertEquals(101, graph.points[6])
            }
        }
    }

    @Test
    fun extendedDraggingSmoothsEightNeighborsAndKeepsEndpoints() {
        ActivityScenario.launch(ProgActivity::class.java).use { scenario ->
            select(scenario, 2)
            scenario.onActivity { activity ->
                val view = screen(activity, true)
                (67..94).forEach { input(view, it).value = 100 }
                input(view, 67).value = 0
                input(view, 94).value = 255
                val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                val inset = 8f * graph.resources.displayMetrics.density
                val plotHeight = graph.height - 2 * inset
                val x = inset + 14 * (graph.width - 2 * inset) / 27
                val y = graph.height - inset - 100f / 255 * plotHeight
                touch(graph, MotionEvent.ACTION_DOWN, x, y)
                touch(graph, MotionEvent.ACTION_MOVE, x, y - 60f / 255 * plotHeight)
                assertEquals(160, input(view, 81).value)
                for ((distance, value) in listOf(159, 155, 147, 136, 124, 113, 105, 101).withIndex()) {
                    assertEquals(value, input(view, 81 - distance - 1).value)
                    assertEquals(value, input(view, 81 + distance + 1).value)
                }
                assertEquals(0, input(view, 67).value)
                assertEquals(255, input(view, 94).value)
                touch(graph, MotionEvent.ACTION_CANCEL, x, y)
                assertTrue(activity.findViewById<ViewPager2>(R.id.progPager).isUserInputEnabled)
            }
        }
    }

    @Test
    fun cancellingReadKeepsEditedValuesAndReenablesControls() {
        ActivityScenario.launch(ProgActivity::class.java).use { scenario ->
            select(scenario, 2)
            var before = emptyList<Int>()
            scenario.onActivity { activity ->
                val view = screen(activity, true)
                input(view, 67).value = 41
                before = view.findViewById<AdjustableCurveView>(R.id.curveGraph).points
                view.findViewById<Button>(R.id.curveRead).performClick()
            }
            androidx.test.espresso.Espresso.onView(
                androidx.test.espresso.matcher.ViewMatchers.withText(android.R.string.cancel)
            ).perform(androidx.test.espresso.action.ViewActions.click())
            waitForTransfer(scenario)
            scenario.onActivity { activity ->
                val view = screen(activity, true)
                assertEquals(before, view.findViewById<AdjustableCurveView>(R.id.curveGraph).points)
                assertTrue(view.findViewById<Button>(R.id.curveRead).isEnabled)
                assertTrue(view.findViewById<Button>(R.id.curveWrite).isEnabled)
                assertTrue(input(view, 67).isEnabled)
            }
        }
    }

    @Test
    fun originalCvTabSupportsMockReadWriteAndBits() {
        ActivityScenario.launch(ProgActivity::class.java).use { scenario ->
            instrumentation.waitForIdleSync()
            scenario.onActivity { activity ->
                activity.findViewById<PlusMinusView>(R.id.plusminusCvNum).value = 17
                activity.findViewById<PlusMinusView>(R.id.plusminusCvValue).value = 123
                activity.findViewById<Button>(R.id.buttonWrite).performClick()
            }
            fun waitForButton() {
                val until = SystemClock.uptimeMillis() + 5000
                while (SystemClock.uptimeMillis() < until) {
                    var ready = false
                    scenario.onActivity { ready = it.findViewById<Button>(R.id.buttonRead).isEnabled }
                    if (ready) return
                    SystemClock.sleep(50)
                }
                fail("Single CV request did not finish")
            }
            waitForButton()
            scenario.onActivity { activity ->
                assertEquals(123, MockStore.programmingCvValue(17))
                activity.findViewById<PlusMinusView>(R.id.plusminusCvValue).value = 0
                activity.findViewById<Button>(R.id.buttonRead).performClick()
            }
            waitForButton()
            scenario.onActivity { activity ->
                assertEquals(123, activity.findViewById<PlusMinusView>(R.id.plusminusCvValue).value)
                val bits = activity.findViewById<LinearLayout>(R.id.layoutBits)
                val binary = (0 until bits.childCount).joinToString("") {
                    if ((bits.getChildAt(it) as android.widget.ToggleButton).isChecked) "1" else "0"
                }
                assertEquals("01111011", binary)
            }
        }
    }

    @Test
    fun screenshotsAndLandscapeHaveIndependentScrollAreas() {
        if (InstrumentationRegistry.getArguments().getString("programmingScreenshots") != "true") return
        // Let transient messages from earlier read/write tests disappear before capturing UI.
        SystemClock.sleep(4000)
        for (night in listOf(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO,
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)) {
            instrumentation.runOnMainSync { androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(night) }
            for (landscape in listOf(false, true)) {
                ActivityScenario.launch(ProgActivity::class.java).use { scenario ->
                    scenario.onActivity {
                        it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                    instrumentation.waitForIdleSync()
                    SystemClock.sleep(400)
                    for (tab in 0..2) {
                        select(scenario, tab)
                        SystemClock.sleep(200)
                        scenario.onActivity { activity ->
                            if (tab > 0) {
                                val view = screen(activity, tab == 2)
                                val toggle = view.findViewById<SwitchMaterial>(R.id.curveSmoothing)
                                assertEquals(if (tab == 2) View.VISIBLE else View.GONE, toggle.visibility)
                                if (tab == 2) {
                                    val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                                    assertTrue(toggle.isChecked)
                                    assertSame(graph.parent, toggle.parent)
                                    val graphPosition = IntArray(2).also { graph.getLocationOnScreen(it) }
                                    val togglePosition = IntArray(2).also { toggle.getLocationOnScreen(it) }
                                    val labelPaint = android.graphics.Paint().apply {
                                        textSize = 10f * graph.resources.displayMetrics.scaledDensity
                                    }
                                    val labelRight = graphPosition[0] + 10f * graph.resources.displayMetrics.density +
                                        labelPaint.measureText(graph.maxValue.toString())
                                    assertTrue("Smoothing toggle must clear the Y-axis label", togglePosition[0] > labelRight)
                                    val inset = (8f * graph.resources.displayMetrics.density + 0.5f).toInt()
                                    assertEquals(graphPosition[0] + graph.width - inset, togglePosition[0] + toggle.width)
                                    assertEquals(graphPosition[1] + graph.height - inset, togglePosition[1] + toggle.height)
                                }
                                if (landscape) {
                                    assertEquals(Configuration.ORIENTATION_LANDSCAPE, activity.resources.configuration.orientation)
                                    assertNotNull(view.findViewById<LockableScrollView>(R.id.curveScroll))
                                    assertNotNull(view.findViewById<LockableScrollView>(R.id.curveControlsScroll))
                                    val scroll = view.findViewById<LockableScrollView>(R.id.curveScroll)
                                    val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                                    assertTrue(graph.fitToContainerHeight)
                                    assertTrue(graph.height > 0)
                                    assertTrue(graph.height <= scroll.height)
                                    assertFalse("Landscape graph should fit without vertical scrolling", scroll.canScrollVertically(1))
                                } else {
                                    assertTrue(view is LockableScrollView)
                                    val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
                                    assertFalse(graph.fitToContainerHeight)
                                    assertEquals(graph.width, graph.height)
                                }
                            }
                        }
                        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "programming-curves-review").apply { mkdirs() }
                        File(directory, "theme-$night-land-$landscape-tab-$tab.png").outputStream().use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                        bitmap.recycle()
                        if (tab == 2) {
                            scenario.onActivity { activity ->
                                val view = screen(activity, true)
                                val scroll = view as? LockableScrollView
                                    ?: view.findViewById<LockableScrollView>(R.id.curveControlsScroll)
                                val title = view.findViewById<View>(R.id.curveTrimTitle)
                                val titlePosition = IntArray(2).also { title.getLocationOnScreen(it) }
                                val scrollPosition = IntArray(2).also { scroll.getLocationOnScreen(it) }
                                scroll.scrollBy(0, titlePosition[1] - scrollPosition[1] - scroll.paddingTop)
                            }
                            instrumentation.waitForIdleSync()
                            SystemClock.sleep(200)
                            val section = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                            File(directory, "theme-$night-land-$landscape-tab-2-trim.png").outputStream().use {
                                section.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                            section.recycle()
                            scenario.onActivity { activity ->
                                val view = screen(activity, true)
                                val scroll = view as? LockableScrollView
                                    ?: view.findViewById<LockableScrollView>(R.id.curveControlsScroll)
                                scroll.fullScroll(View.FOCUS_DOWN)
                            }
                            instrumentation.waitForIdleSync()
                            SystemClock.sleep(200)
                            val bottom = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                            File(directory, "theme-$night-land-$landscape-tab-2-bottom.png").outputStream().use {
                                bottom.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                            bottom.recycle()
                        }
                    }
                }
            }
        }
        instrumentation.runOnMainSync { androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
    }
}
