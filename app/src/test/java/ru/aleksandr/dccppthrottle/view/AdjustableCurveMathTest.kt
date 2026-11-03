package ru.aleksandr.dccppthrottle.view

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.aleksandr.dccppthrottle.view.AdjustableCurveView.AdjustableCurveMath

class AdjustableCurveMathTest {
    @Test
    fun influenceDecaysLinearlyAndEndpointsStayFixed() {
        assertEquals(
            listOf(0, 50, 60, 90, 60, 50, 255),
            AdjustableCurveMath.edit(listOf(0, 40, 40, 60, 40, 40, 255), 3, 90, 0, 255, 2)
        )
    }

    @Test
    fun neighborsApproachTargetFromBothDirectionsWithoutOvershooting() {
        assertEquals(
            listOf(0, 87, 90, 93, 255),
            AdjustableCurveMath.edit(listOf(0, 80, 60, 100, 255), 2, 90, 0, 255, 2)
        )
        assertEquals(
            listOf(0, 73, 70, 73, 255),
            AdjustableCurveMath.edit(listOf(0, 80, 100, 80, 255), 2, 70, 0, 255, 2)
        )
    }

    @Test
    fun noMovementPreservesAllValuesAndSmallMovementsHaveLimitedInfluence() {
        val initial = listOf(0, 20, 100, 180, 255)
        assertEquals(initial, AdjustableCurveMath.edit(initial, 2, 100, 0, 255, 2))
        assertEquals(listOf(0, 21, 101, 179, 255), AdjustableCurveMath.edit(initial, 2, 101, 0, 255, 2))
        // Every event uses the same baseline, so intermediate events do not accumulate rounding.
        AdjustableCurveMath.edit(initial, 2, 150, 0, 255, 2)
        assertEquals(initial, AdjustableCurveMath.edit(initial, 2, 100, 0, 255, 2))
    }

    @Test
    fun smoothingIsDisabledForAtMostThreePointsOrZeroRadius() {
        assertEquals(listOf(0, 200, 255), AdjustableCurveMath.edit(listOf(0, 100, 255), 1, 200, 0, 255, 5))
        assertEquals(listOf(0, 200, 150, 255), AdjustableCurveMath.edit(listOf(0, 100, 150, 255), 1, 200, 0, 255, 0))
        assertEquals(listOf(7), AdjustableCurveMath.edit(listOf(0), 0, 7, 0, 255, 2))
        assertEquals(listOf(7, 255), AdjustableCurveMath.edit(listOf(0, 255), 0, 7, 0, 255, 2))
    }

    @Test
    fun endpointsCanBeEditedDirectlyAndInfluenceInteriorPoints() {
        assertEquals(listOf(60, 53, 47, 255), AdjustableCurveMath.edit(listOf(0, 40, 40, 255), 0, 60, 0, 255, 2))
        assertEquals(listOf(0, 40, 55, 90), AdjustableCurveMath.edit(listOf(0, 40, 40, 60), 3, 90, 0, 255, 1))
    }

    @Test
    fun radiusAndValuesAreClamped() {
        assertEquals(0, AdjustableCurveMath.smoothingRadius(3, 3))
        assertEquals(2, AdjustableCurveMath.smoothingRadius(8, 5))
        assertEquals(0, AdjustableCurveMath.smoothingRadius(-1, 5))
        assertEquals(listOf(10, 20, 10, 20), AdjustableCurveMath.edit(listOf(-1, 15, -200, 300), 1, 500, 10, 20, 0))
    }

    @Test
    fun roundingIsSymmetricAtHalfSteps() {
        assertEquals(1, AdjustableCurveMath.round(0.5))
        assertEquals(-1, AdjustableCurveMath.round(-0.5))
        assertEquals(listOf(0, 11, 19, 255), AdjustableCurveMath.edit(listOf(0, 10, 20, 255), 1, 11, 0, 255, 1))
        assertEquals(listOf(0, 9, 19, 255), AdjustableCurveMath.edit(listOf(0, 10, 20, 255), 1, 9, 0, 255, 1))
    }

    @Test
    fun cubicInterpolationIsContinuousAndDoesNotOvershootBetweenMarkers() {
        listOf(
            listOf(0, 10, 255, 255), listOf(255, 0, 255, 0), listOf(0, 200, 210, 255),
            listOf(255, 210, 200, 0), listOf(50, 50, 50, 50)
        ).forEach { values ->
            val slopes = AdjustableCurveMath.tangents(values)
            for (i in 0 until values.lastIndex) {
                for (sample in 0..100) {
                    val t = sample / 100.0
                    val t2 = t * t
                    val t3 = t2 * t
                    val y = (2 * t3 - 3 * t2 + 1) * values[i] +
                        (t3 - 2 * t2 + t) * slopes[i] +
                        (-2 * t3 + 3 * t2) * values[i + 1] + (t3 - t2) * slopes[i + 1]
                    assertTrue(y >= minOf(values[i], values[i + 1]) - 1e-8)
                    assertTrue(y <= maxOf(values[i], values[i + 1]) + 1e-8)
                }
            }
        }
        assertArrayEquals(doubleArrayOf(), AdjustableCurveMath.tangents(emptyList()), 0.0)
        assertArrayEquals(doubleArrayOf(0.0), AdjustableCurveMath.tangents(listOf(10)), 0.0)
    }
}
