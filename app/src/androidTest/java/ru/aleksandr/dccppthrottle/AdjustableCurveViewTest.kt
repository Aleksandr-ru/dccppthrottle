package ru.aleksandr.dccppthrottle

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Parcelable
import android.util.SparseArray
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import ru.aleksandr.dccppthrottle.view.AdjustableCurveView
import java.io.File

@RunWith(AndroidJUnit4::class)
class AdjustableCurveViewTest {
    private fun inBothThemes(check: (AdjustableCurveView) -> Unit) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            for (night in listOf(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES)) {
                val context = instrumentation.targetContext
                val configuration = Configuration(context.resources.configuration).apply {
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or night
                }
                val themed = ContextThemeWrapper(
                    context.createConfigurationContext(configuration), R.style.Theme_DCCppThrottle
                )
                check(AdjustableCurveView(themed))
            }
        }
    }

    private fun layout(view: AdjustableCurveView) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun touch(view: AdjustableCurveView, action: Int, x: Float, y: Float): Boolean {
        val event = MotionEvent.obtain(0, 0, action, x, y, 0)
        return try { view.onTouchEvent(event) } finally { event.recycle() }
    }

    private fun draw(view: AdjustableCurveView, name: String) {
        val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(com.google.android.material.color.MaterialColors.getColor(
            view, com.google.android.material.R.attr.colorSurface, android.graphics.Color.WHITE
        ))
        view.draw(canvas)
        if (InstrumentationRegistry.getArguments().getString("curveScreenshots") == "true") {
            val night = view.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val directory = File(view.context.getExternalFilesDir(null), "adjustable-curve-review").apply { mkdirs() }
            File(directory, "$name-$night.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        bitmap.recycle()
    }

    @Test
    fun defaultsSquareMeasurementClampingAndStateRestoration() = inBothThemes { view ->
        assertEquals(0, view.minValue)
        assertEquals(255, view.maxValue)
        assertEquals(127, view.gridStep)
        assertEquals(0, view.smoothing)
        assertTrue(view.pointLabels.isEmpty())
        layout(view)
        assertEquals(600, view.measuredWidth)
        assertEquals(600, view.measuredHeight)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(700, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        assertEquals(700, view.measuredWidth)
        assertEquals(700, view.measuredHeight)
        view.id = R.id.plusminusCvValue
        view.setValueRange(10, 100)
        view.points = listOf(-10, 20, 50, 80, 500)
        view.pointLabels = listOf("CV67", "CV68", "", "CV70", "CV71")
        view.smoothing = 10
        view.gridStep = 30
        assertEquals(listOf(10, 20, 50, 80, 100), view.points)
        assertEquals(2, view.smoothing)
        val state = SparseArray<Parcelable>()
        view.saveHierarchyState(state)
        val restored = AdjustableCurveView(view.context).apply { id = view.id }
        var notifications = 0
        restored.setOnChangeListener { notifications++ }
        restored.restoreHierarchyState(state)
        assertEquals(view.points, restored.points)
        assertEquals(view.pointLabels, restored.pointLabels)
        assertEquals(10, restored.minValue)
        assertEquals(100, restored.maxValue)
        assertEquals(30, restored.gridStep)
        assertEquals(2, restored.smoothing)
        assertEquals(0, notifications)
    }

    @Test
    fun optionalHeightFollowsContainerIncludingPaddedScrollViewport() = inBothThemes { view ->
        assertFalse(view.fitToContainerHeight)
        val width = View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY)
        val tallHeight = View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.AT_MOST)
        view.measure(width, tallHeight)
        assertEquals(600, view.measuredHeight)
        view.fitToContainerHeight = true
        view.measure(width, tallHeight)
        assertEquals(900, view.measuredHeight)
        // With no height hint, keep the square fallback.
        layout(view)
        assertEquals(600, view.measuredHeight)

        val content = android.widget.LinearLayout(view.context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(10, 20, 30, 40)
            addView(view, android.widget.LinearLayout.LayoutParams(-1, -2))
        }
        val scroll = android.widget.ScrollView(view.context).apply {
            isFillViewport = true
            setPadding(7, 11, 13, 17)
            addView(content, android.widget.FrameLayout.LayoutParams(-1, -2))
        }
        fun measureScroll() {
            scroll.measure(width, View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY))
            scroll.layout(0, 0, scroll.measuredWidth, scroll.measuredHeight)
        }
        measureScroll()
        assertEquals(540, view.measuredWidth)
        assertEquals(212, view.measuredHeight)
        assertFalse(scroll.canScrollVertically(1))
        view.fitToContainerHeight = false
        measureScroll()
        assertEquals(540, view.measuredHeight)
        assertTrue(scroll.canScrollVertically(1))
    }

    @Test
    fun dragBubbleAddsLabelLineOnlyForNonblankLabels() = inBothThemes { view ->
        view.points = List(3) { 128 }
        layout(view)
        assertTrue(touch(view, MotionEvent.ACTION_DOWN, 300f, 300f))
        fun render(): Bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888).also {
            view.draw(Canvas(it))
        }
        val numberOnly = render()
        for (labels in listOf(listOf("CV2"), listOf("CV2", ""), listOf("CV2", "  "))) {
            view.pointLabels = labels
            val image = render()
            assertTrue("Missing or blank labels must not add an empty line", numberOnly.sameAs(image))
            image.recycle()
        }
        draw(view, "label-blank")
        val labels = mutableListOf("CV2", "CV6", "CV5")
        view.pointLabels = labels
        labels[1] = "changed"
        assertEquals("CV6", view.pointLabels[1])
        val labeled = render()
        val accent = com.google.android.material.color.MaterialColors.getColor(
            view, com.google.android.material.R.attr.colorPrimary, android.graphics.Color.BLUE
        )
        fun bubbleHeight(bitmap: Bitmap): Int {
            var top = bitmap.height
            var bottom = -1
            // The horizontal curve and markers are below this region.
            val limit = (300f - 8f * view.resources.displayMetrics.density).toInt()
            for (y in 0 until limit) for (x in 0 until bitmap.width) {
                if (bitmap.getPixel(x, y) == accent) {
                    top = minOf(top, y)
                    bottom = maxOf(bottom, y)
                }
            }
            assertTrue(bottom >= top)
            return bottom - top + 1
        }
        val lineHeight = android.graphics.Paint().apply {
            textSize = 12f * view.resources.displayMetrics.scaledDensity
        }.fontSpacing
        assertTrue(kotlin.math.abs(bubbleHeight(labeled) - bubbleHeight(numberOnly) - lineHeight) <= 2f)
        draw(view, "label-cv")
        touch(view, MotionEvent.ACTION_UP, 300f, 300f)
        numberOnly.recycle()
        labeled.recycle()
    }

    @Test
    fun draggingClampsValuesSmoothsNeighborsAndKeepsOppositeEndpointFixed() = inBothThemes { view ->
        view.points = List(5) { 128 }
        view.pointLabels = (67..71).map { "CV$it" }
        view.smoothing = 2
        layout(view)
        var changes = emptyList<Int>()
        view.setOnChangeListener { changes = it }
        val x = view.width - 8f * view.resources.displayMetrics.density
        assertTrue(touch(view, MotionEvent.ACTION_DOWN, x, 300f))
        assertTrue(touch(view, MotionEvent.ACTION_MOVE, x + 100f, -1000f))
        assertEquals(listOf(128, 128, 155, 228, 255), view.points)
        assertEquals(view.points, changes)
        // The value bubble at the top marker must also render without clipping errors.
        draw(view, "drag-top")
        touch(view, MotionEvent.ACTION_CANCEL, x, -1000f)
        val before = view.points
        assertFalse(touch(view, MotionEvent.ACTION_MOVE, x, 1000f))
        assertEquals(before, view.points)
        view.isEnabled = false
        assertFalse(touch(view, MotionEvent.ACTION_DOWN, x, 0f))
    }

    @Test
    fun emptyAndSinglePointCurvesCanBeDrawnAndValuesAreCopied() = inBothThemes { view ->
        layout(view)
        draw(view, "empty")
        assertFalse(touch(view, MotionEvent.ACTION_DOWN, 300f, 300f))
        view.points = listOf(0, 40, 80, 120, 160, 200, 255)
        draw(view, "curve")
        view.points = List(28) { 100 }
        view.smoothing = 8
        val inset = 8f * view.resources.displayMetrics.density
        val plotHeight = view.height - 2 * inset
        val x = inset + 14 * (view.width - 2 * inset) / 27
        val y = view.height - inset - 100f / 255 * plotHeight
        assertTrue(touch(view, MotionEvent.ACTION_DOWN, x, y))
        assertTrue(touch(view, MotionEvent.ACTION_MOVE, x, y - 80f / 255 * plotHeight))
        draw(view, "smooth-flat-drag")
        assertTrue(touch(view, MotionEvent.ACTION_UP, x, y - 80f / 255 * plotHeight))
        draw(view, "smooth-flat")
        val input = mutableListOf(100)
        view.points = input
        input[0] = 200
        assertEquals(listOf(100), view.points)
        view.smoothing = 2
        assertEquals(0, view.smoothing)
        draw(view, "single")
        view.setPadding(400, 400, 400, 400)
        draw(view, "small")
    }
}
