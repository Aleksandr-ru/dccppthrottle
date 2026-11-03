package ru.aleksandr.dccppthrottle.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import ru.aleksandr.dccppthrottle.R
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/** Width-driven curve editor, square by default. [points] are integer Y values. */
class AdjustableCurveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var minimum = 0
    private var maximum = 255
    private var values = emptyList<Int>()
    private var labels = emptyList<String>()
    private var radius = 0

    /** Fill the parent's available height instead of deriving height from width. */
    var fitToContainerHeight: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
        }

    var minValue: Int
        get() = minimum
        set(value) = setValueRange(value, maximum)

    var maxValue: Int
        get() = maximum
        set(value) = setValueRange(minimum, value)

    var gridStep: Int = 127
        set(value) {
            require(value > 0) { "gridStep must be positive" }
            field = value
            invalidate()
        }

    var points: List<Int>
        get() = values.toList()
        set(value) {
            finishDrag()
            values = value.map { it.coerceIn(minimum, maximum) }
            radius = AdjustableCurveMath.smoothingRadius(radius, values.size)
            invalidate()
        }

    /** Optional labels matched to [points] by index; missing or blank labels are hidden. */
    var pointLabels: List<String>
        get() = labels.toList()
        set(value) {
            labels = value.toList()
            invalidate()
        }

    var smoothing: Int
        get() = radius
        set(value) {
            finishDrag()
            radius = AdjustableCurveMath.smoothingRadius(value, values.size)
        }

    private var onChangeListener: ((List<Int>) -> Unit)? = null
    private var onDragStateChangeListener: ((Boolean) -> Unit)? = null
    private val plot = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val density = resources.displayMetrics.density
    /** Visible point radius in pixels; the touch target remains 24dp. */
    var pointRadius = 7f * density
        set(value) {
            require(value.isFinite() && value > 0f) { "pointRadius must be positive and finite" }
            field = value
            invalidate()
        }
    private val touchRadius = 24f * density
    private val textSize = 12f * resources.displayMetrics.scaledDensity
    private val axisTextSize = 10f * resources.displayMetrics.scaledDensity
    private val gap = 6f * density
    private val valueLabelOffset = 32f * density
    private val foreground = MaterialColors.getColor(
        this, com.google.android.material.R.attr.colorOnSurface, Color.DKGRAY
    )
    private val accent = MaterialColors.getColor(
        this, com.google.android.material.R.attr.colorPrimary, Color.BLUE
    )
    private val onAccent = MaterialColors.getColor(
        this, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE
    )
    private var selected = -1
    private var pointer = MotionEvent.INVALID_POINTER_ID
    private var dragOffset = 0f
    private var dragStart = emptyList<Int>()

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.AdjustableCurveView, defStyleAttr, 0)
        try {
            setValueRange(
                attributes.getInt(R.styleable.AdjustableCurveView_min, 0),
                attributes.getInt(R.styleable.AdjustableCurveView_max, 255)
            )
            gridStep = attributes.getInt(R.styleable.AdjustableCurveView_gridStep, 127)
            val pointsId = attributes.getResourceId(R.styleable.AdjustableCurveView_points, 0)
            if (pointsId != 0) points = resources.getIntArray(pointsId).toList()
            val labelsId = attributes.getResourceId(R.styleable.AdjustableCurveView_pointLabels, 0)
            if (labelsId != 0) pointLabels = resources.getStringArray(labelsId).toList()
            smoothing = attributes.getInt(R.styleable.AdjustableCurveView_smoothing, 0)
            fitToContainerHeight = attributes.getBoolean(R.styleable.AdjustableCurveView_fitToContainerHeight, false)
            pointRadius = attributes.getDimension(R.styleable.AdjustableCurveView_pointRadius, pointRadius)
        } finally {
            attributes.recycle()
        }
        isClickable = true
    }

    /** Updates both bounds atomically, including ranges outside the default 0..255. */
    fun setValueRange(minValue: Int, maxValue: Int) {
        require(minValue < maxValue) { "minValue must be less than maxValue" }
        finishDrag()
        minimum = minValue
        maximum = maxValue
        values = values.map { it.coerceIn(minimum, maximum) }
        invalidate()
    }

    /** Called for user edits (including smoothing); property assignments do not notify. */
    fun setOnChangeListener(listener: ((List<Int>) -> Unit)?) {
        onChangeListener = listener
    }

    /** Lets a host suspend scrolling/paging only while a marker is being dragged. */
    fun setOnDragStateChangeListener(listener: ((Boolean) -> Unit)?) {
        onDragStateChangeListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = max(suggestedMinimumWidth, (240 * density).toInt())
        val width = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) desired
            else MeasureSpec.getSize(widthMeasureSpec)
        val availableHeight = MeasureSpec.getSize(heightMeasureSpec)
        // ScrollView passes its viewport height as an UNSPECIFIED size hint. In fit mode
        // use that hint too, so the curve cannot make its scroll container taller.
        val height = if (fitToContainerHeight && availableHeight > 0) availableHeight
            else resolveSize(width, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private fun updatePlot() {
        paint.textSize = axisTextSize
        val inset = pointRadius + density
        plot.set(
            paddingLeft + inset,
            paddingTop + max(inset, axisTextSize / 2),
            width - paddingRight - inset,
            height - paddingBottom - max(inset, axisTextSize / 2)
        )
    }

    private fun x(index: Int): Float = if (values.size <= 1) plot.left
        else plot.left + plot.width() * index / values.lastIndex

    private fun y(value: Double): Float =
        (plot.bottom - (value - minimum) / (maximum.toDouble() - minimum) * plot.height()).toFloat()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updatePlot()
        if (plot.width() <= 0 || plot.height() <= 0) return
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = density
        paint.color = ColorUtils.setAlphaComponent(foreground, if (isEnabled) 45 else 25)
        // Draw only visible grid lines when a very large value range is configured.
        val count = (maximum.toLong() - minimum) / gridStep
        val stride = max(1L, kotlin.math.ceil(count / max(1f, plot.height()).toDouble()).toLong())
        var line = stride
        while (line <= count) {
            val value = minimum.toLong() + line * gridStep
            val gridY = y(value.toDouble())
            canvas.drawLine(plot.left, gridY, plot.right, gridY, paint)
            line += stride
        }
        paint.color = ColorUtils.setAlphaComponent(foreground, if (isEnabled) 210 else 90)
        canvas.drawLine(plot.left, plot.bottom, plot.right, plot.bottom, paint)
        canvas.drawLine(plot.left, plot.top, plot.left, plot.bottom, paint)
        paint.style = Paint.Style.FILL
        paint.textSize = axisTextSize
        paint.textAlign = Paint.Align.LEFT
        val labelX = plot.left + 2f * density
        // Keep the labels just inside the Y axis.
        canvas.drawText(maximum.toString(), labelX, plot.top - paint.ascent(), paint)
        canvas.drawText(minimum.toString(), labelX, plot.bottom - paint.descent(), paint)

        paint.color = ColorUtils.setAlphaComponent(accent, if (isEnabled) 255 else 90)
        if (values.size > 1) {
            val tangents = AdjustableCurveMath.tangents(values)
            val stepX = plot.width() / values.lastIndex
            path.reset()
            path.moveTo(x(0), y(values[0].toDouble()))
            for (i in 0 until values.lastIndex) {
                path.cubicTo(
                    x(i) + stepX / 3, y(values[i] + tangents[i] / 3),
                    x(i + 1) - stepX / 3, y(values[i + 1] - tangents[i + 1] / 3),
                    x(i + 1), y(values[i + 1].toDouble())
                )
            }
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f * density
            canvas.drawPath(path, paint)
        }
        paint.style = Paint.Style.FILL
        values.forEachIndexed { i, value ->
            canvas.drawCircle(x(i), y(value.toDouble()), pointRadius, paint)
        }
        if (selected in values.indices) drawValueLabel(canvas)
    }

    private fun drawValueLabel(canvas: Canvas) {
        val text = values[selected].toString()
        val pointLabel = labels.getOrNull(selected)?.takeIf { it.isNotBlank() }
        paint.textSize = textSize
        val labelWidth = max(paint.measureText(text), pointLabel?.let { paint.measureText(it) } ?: 0f) + 2 * gap
        val lineHeight = paint.fontSpacing
        val labelHeight = lineHeight * (if (pointLabel != null) 2 else 1) + 2 * gap
        val pointY = y(values[selected].toDouble())
        val left = (x(selected) - labelWidth / 2).coerceIn(0f, max(0f, width - labelWidth))
        var top = pointY - pointRadius - valueLabelOffset - labelHeight
        if (top < paddingTop) top = pointY + pointRadius + valueLabelOffset
        val label = RectF(left, top, left + labelWidth, top + labelHeight)
        paint.color = accent
        canvas.drawRoundRect(label, gap, gap, paint)
        paint.color = onAccent
        paint.textAlign = Paint.Align.CENTER
        var baseline = label.top + gap - paint.ascent()
        if (pointLabel != null) {
            canvas.drawText(pointLabel, label.centerX(), baseline, paint)
            baseline += lineHeight
        }
        canvas.drawText(text, label.centerX(), baseline, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            finishDrag()
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                updatePlot()
                if (plot.width() <= 0 || plot.height() <= 0) return false
                selected = values.indices.filter {
                    abs(event.x - x(it)) <= touchRadius && abs(event.y - y(values[it].toDouble())) <= touchRadius
                }.minByOrNull {
                    val dx = event.x - x(it)
                    val dy = event.y - y(values[it].toDouble())
                    dx * dx + dy * dy
                } ?: return false
                pointer = event.getPointerId(0)
                dragOffset = event.y - y(values[selected].toDouble())
                dragStart = values.toList()
                parent?.requestDisallowInterceptTouchEvent(true)
                onDragStateChangeListener?.invoke(true)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (selected < 0) return false
                val pointerIndex = event.findPointerIndex(pointer)
                if (pointerIndex < 0) {
                    finishDrag()
                    return true
                }
                updateDrag(event.getY(pointerIndex))
            }
            MotionEvent.ACTION_UP -> {
                if (selected < 0) return false
                val pointerIndex = event.findPointerIndex(pointer)
                if (pointerIndex >= 0) updateDrag(event.getY(pointerIndex))
                finishDrag()
                performClick()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == pointer) finishDrag()
            }
            MotionEvent.ACTION_CANCEL -> finishDrag()
        }
        return selected >= 0 || event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_CANCEL
    }

    private fun updateDrag(touchY: Float) {
        val fraction = ((plot.bottom - touchY + dragOffset) / plot.height()).coerceIn(0f, 1f)
        val target = AdjustableCurveMath.round(minimum + fraction.toDouble() * (maximum.toDouble() - minimum))
        val updated = AdjustableCurveMath.edit(dragStart, selected, target, minimum, maximum, radius)
        if (updated != values) {
            values = updated
            invalidate()
            onChangeListener?.invoke(values.toList())
        }
    }

    private fun finishDrag() {
        val wasDragging = selected >= 0
        selected = -1
        pointer = MotionEvent.INVALID_POINTER_ID
        dragStart = emptyList()
        parent?.requestDisallowInterceptTouchEvent(false)
        if (wasDragging) onDragStateChangeListener?.invoke(false)
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        finishDrag()
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable("superState", super.onSaveInstanceState())
        putInt("min", minimum)
        putInt("max", maximum)
        putInt("gridStep", gridStep)
        putIntArray("points", values.toIntArray())
        putStringArray("pointLabels", labels.toTypedArray())
        putInt("smoothing", smoothing)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("superState"))
            setValueRange(state.getInt("min"), state.getInt("max", 255))
            gridStep = state.getInt("gridStep", 127)
            points = state.getIntArray("points")?.toList() ?: emptyList()
            pointLabels = state.getStringArray("pointLabels")?.toList() ?: emptyList()
            smoothing = state.getInt("smoothing")
        } else super.onRestoreInstanceState(state)
    }

    /** Pure calculations shared by touch editing and tests. */
    internal object AdjustableCurveMath {
        fun smoothingRadius(requested: Int, count: Int): Int =
            if (count <= 3) 0 else requested.coerceIn(0, count / 2)

        // Round ties away from zero, including negative changes.
        fun round(value: Double): Int = (sign(value) * floor(abs(value) + 0.5)).toInt()

        fun edit(
            initial: List<Int>, index: Int, value: Int, minValue: Int, maxValue: Int, smoothing: Int
        ): List<Int> {
            require(minValue < maxValue)
            require(index in initial.indices)
            val baseline = initial.map { it.coerceIn(minValue, maxValue) }
            val target = value.coerceIn(minValue, maxValue)
            val movement = target.toDouble() - baseline[index]
            val radius = smoothingRadius(smoothing, baseline.size)
            return baseline.mapIndexed { neighbor, original ->
                val distance = abs(neighbor - index)
                when {
                    neighbor == index -> target
                    neighbor == 0 || neighbor == baseline.lastIndex || distance > radius -> original
                    else -> {
                        // Taper to the nearest fixed endpoint or the first unaffected point.
                        val span = min(radius + 1, if (neighbor < index) index else baseline.lastIndex - index)
                        val t = distance.toDouble() / span
                        // Quintic falloff: zero first and second derivatives at both ends.
                        val weight = (1.0 - t * t * t * (10.0 - 15.0 * t + 6.0 * t * t)).coerceIn(0.0, 1.0)
                        val change = (sign(movement) * floor(abs(movement) * weight + 0.5)).toLong()
                        (original.toLong() + change).coerceIn(minValue.toLong(), maxValue.toLong()).toInt()
                    }
                }
            }
        }

        /** Slopes for equally spaced monotone cubic Hermite interpolation (x step = 1). */
        fun tangents(values: List<Int>): DoubleArray {
            if (values.size < 2) return DoubleArray(values.size)
            val slopes = DoubleArray(values.size - 1) { values[it + 1].toDouble() - values[it] }
            return DoubleArray(values.size) { index ->
                when (index) {
                    0 -> slopes.first()
                    values.lastIndex -> slopes.last()
                    else -> {
                        val left = slopes[index - 1]
                        val right = slopes[index]
                        if (left == 0.0 || right == 0.0 || sign(left) != sign(right)) 0.0
                        else 2.0 * left * right / (left + right)
                    }
                }
            }
        }
    }
}
