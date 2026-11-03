package ru.aleksandr.dccppthrottle.view

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sign

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
        val movement = abs(target.toDouble() - baseline[index])
        val radius = smoothingRadius(smoothing, baseline.size)
        return baseline.mapIndexed { neighbor, original ->
            val distance = abs(neighbor - index)
            when {
                neighbor == index -> target
                neighbor == 0 || neighbor == baseline.lastIndex || distance > radius -> original
                else -> {
                    val gap = target.toDouble() - original
                    val weight = (radius + 1.0 - distance) / (radius + 1.0)
                    val change = (sign(gap) * floor(min(abs(gap), movement) * weight + 0.5)).toLong()
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
