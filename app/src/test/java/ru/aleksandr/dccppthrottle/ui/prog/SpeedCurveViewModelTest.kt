package ru.aleksandr.dccppthrottle.ui.prog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedCurveViewModelTest {
    @Test
    fun simplePointsFollowStartMidMaximumOrder() {
        assertEquals(listOf(2, 6, 5), SpeedCurveViewModel.pointCvs(false))
        assertEquals(listOf(29, 2, 6, 5), SpeedCurveViewModel.readCvs(false))
    }

    @Test
    fun extendedTableHasExactly28PointsAndSeparateTrims() {
        assertEquals((67..94).toList(), SpeedCurveViewModel.pointCvs(true))
        assertEquals((67..94).toList() + listOf(66, 95), SpeedCurveViewModel.dataCvs(true))
        assertEquals(31, SpeedCurveViewModel.readCvs(true).distinct().size)
    }

    @Test
    fun selectingCurvePreservesEveryOtherCv29BitForAllByteValues() {
        for (value in 0..255) {
            val simple = SpeedCurveViewModel.withSimpleEnabled(value, true)
            val extended = SpeedCurveViewModel.withSimpleEnabled(value, false)
            assertEquals(value and 0xEF, simple)
            assertEquals(value or 0x10, extended)
            assertTrue(SpeedCurveViewModel.simpleEnabled(simple))
            assertFalse(SpeedCurveViewModel.simpleEnabled(extended))
        }
    }
}
