package ru.aleksandr.dccppthrottle.view

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.aleksandr.dccppthrottle.view.AdjustableCurveView.AdjustableCurveMath

class AdjustableCurveMathTest {
    @Test
    fun displacementTapersSmoothlyAndEndpointsStayFixed() {
        assertEquals(
            listOf(0, 46, 64, 90, 64, 46, 255),
            AdjustableCurveMath.edit(listOf(0, 40, 40, 60, 40, 40, 255), 3, 90, 0, 255, 2)
        )
    }

    @Test
    fun neighborsFollowDisplacementPreservingTheOriginalShape() {
        assertEquals(
            listOf(0, 95, 90, 115, 255),
            AdjustableCurveMath.edit(listOf(0, 80, 60, 100, 255), 2, 90, 0, 255, 2)
        )
        assertEquals(
            listOf(0, 65, 70, 65, 255),
            AdjustableCurveMath.edit(listOf(0, 80, 100, 80, 255), 2, 70, 0, 255, 2)
        )
    }

    @Test
    fun noMovementPreservesAllValuesAndSmallMovementsHaveLimitedInfluence() {
        val initial = listOf(0, 20, 100, 180, 255)
        assertEquals(initial, AdjustableCurveMath.edit(initial, 2, 100, 0, 255, 2))
        assertEquals(listOf(0, 21, 101, 181, 255), AdjustableCurveMath.edit(initial, 2, 101, 0, 255, 2))
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
        assertEquals(listOf(60, 87, 53, 255), AdjustableCurveMath.edit(listOf(0, 40, 40, 255), 0, 60, 0, 255, 2))
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
        assertEquals(listOf(0, 11, 21, 255), AdjustableCurveMath.edit(listOf(0, 10, 20, 255), 1, 11, 0, 255, 1))
        assertEquals(listOf(0, 9, 19, 255), AdjustableCurveMath.edit(listOf(0, 10, 20, 255), 1, 9, 0, 255, 1))
    }

    @Test
    fun pullingAFlatCurveMakesARoundedCrestWithSoftShoulders() {
        val initial = List(29) { 100 }
        val raised = AdjustableCurveMath.edit(initial, 14, 180, 0, 255, 8)
        val lowered = AdjustableCurveMath.edit(initial, 14, 20, 0, 255, 8)
        assertEquals(listOf(101, 106, 117, 132, 148, 163, 174, 179, 180,
            179, 174, 163, 148, 132, 117, 106, 101), raised.subList(6, 23))
        assertEquals(raised, raised.reversed())
        assertEquals(initial.take(6), raised.take(6))
        assertEquals(initial.takeLast(6), raised.takeLast(6))
        raised.indices.forEach { assertEquals(200, raised[it] + lowered[it]) }
        // Adjacent slopes change gradually, including the crest and both window boundaries.
        for (i in 1 until raised.lastIndex) {
            assertTrue(kotlin.math.abs(raised[i + 1] - 2 * raised[i] + raised[i - 1]) <= 6)
        }
        assertEquals(1, raised[14] - raised[13])
        assertEquals(1, raised[6] - raised[5])
    }

    @Test
    fun straightCurveRetainsGentleChangesOfSlopeAfterDragging() {
        val initial = List(28) { it * 8 }
        val edited = AdjustableCurveMath.edit(initial, 14, 192, 0, 255, 8)
        assertEquals(192, edited[14])
        assertEquals(initial.first(), edited.first())
        assertEquals(initial.last(), edited.last())
        for (i in 1 until edited.lastIndex) {
            assertTrue(kotlin.math.abs(edited[i + 1] - 2 * edited[i] + edited[i - 1]) <= 6)
        }
    }

    @Test
    fun influenceTapersToNearbyFixedEndpointsAndValuesStayInRange() {
        // The nearby endpoint shortens the left side of the smoothing window.
        assertEquals(listOf(100, 140, 180, 175, 155, 125, 105, 100, 100, 100, 100, 100),
            AdjustableCurveMath.edit(List(12) { 100 }, 2, 180, 0, 255, 4))
        for (index in 0..27) {
            for (target in listOf(0, 255)) {
                val result = AdjustableCurveMath.edit(List(28) { 200 }, index, target, 0, 255, 14)
                assertEquals(target, result[index])
                assertTrue(result.all { it in 0..255 })
                if (index != 0) assertEquals(200, result.first())
                if (index != 27) assertEquals(200, result.last())
            }
        }
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
