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
import kotlin.math.max
import kotlin.math.min

/** Square, width-driven editor. [points] are integer Y values; X positions are fixed. */
class AdjustableCurveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var minimum = 0
    private var maximum = 255
    private var values = emptyList<Int>()
    private var radius = 0

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

    var smoothing: Int
        get() = radius
        set(value) {
            finishDrag()
            radius = AdjustableCurveMath.smoothingRadius(value, values.size)
        }

    private var onChangeListener: ((List<Int>) -> Unit)? = null
    private val plot = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val density = resources.displayMetrics.density
    private val markerRadius = 7f * density
    private val touchRadius = 24f * density
    private val textSize = 12f * resources.displayMetrics.scaledDensity
    private val axisTextSize = 10f * resources.displayMetrics.scaledDensity
    private val gap = 6f * density
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
            smoothing = attributes.getInt(R.styleable.AdjustableCurveView_smoothing, 0)
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = max(suggestedMinimumWidth, (240 * density).toInt())
        val width = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) desired
            else MeasureSpec.getSize(widthMeasureSpec)
        // Width determines height. A stricter parent height constraint still takes precedence.
        setMeasuredDimension(width, resolveSize(width, heightMeasureSpec))
    }

    private fun updatePlot() {
        paint.textSize = axisTextSize
        val inset = markerRadius + density
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
            canvas.drawCircle(x(i), y(value.toDouble()), markerRadius, paint)
        }
        if (selected in values.indices) drawValueLabel(canvas)
    }

    private fun drawValueLabel(canvas: Canvas) {
        val text = values[selected].toString()
        paint.textSize = textSize
        val labelWidth = paint.measureText(text) + 2 * gap
        val labelHeight = paint.descent() - paint.ascent() + 2 * gap
        val pointY = y(values[selected].toDouble())
        val left = (x(selected) - labelWidth / 2).coerceIn(0f, max(0f, width - labelWidth))
        var top = pointY - markerRadius - gap - labelHeight
        if (top < paddingTop) top = pointY + markerRadius + gap
        val label = RectF(left, top, left + labelWidth, top + labelHeight)
        paint.color = accent
        canvas.drawRoundRect(label, gap, gap, paint)
        paint.color = onAccent
        canvas.drawText(text, label.left + gap, label.top + gap - paint.ascent(), paint)
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
        selected = -1
        pointer = MotionEvent.INVALID_POINTER_ID
        dragStart = emptyList()
        parent?.requestDisallowInterceptTouchEvent(false)
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
        putInt("smoothing", smoothing)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("superState"))
            setValueRange(state.getInt("min"), state.getInt("max", 255))
            gridStep = state.getInt("gridStep", 127)
            points = state.getIntArray("points")?.toList() ?: emptyList()
            smoothing = state.getInt("smoothing")
        } else super.onRestoreInstanceState(state)
    }
}
