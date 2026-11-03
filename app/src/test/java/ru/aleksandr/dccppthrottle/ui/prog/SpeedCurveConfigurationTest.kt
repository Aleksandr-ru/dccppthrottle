package ru.aleksandr.dccppthrottle.ui.prog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedCurveConfigurationTest {
    @Test
    fun simplePointsFollowStartMidMaximumOrder() {
        assertEquals(listOf(2, 6, 5), SpeedCurveConfiguration.pointCvs(false))
        assertEquals(listOf(29, 2, 6, 5), SpeedCurveConfiguration.readCvs(false))
    }

    @Test
    fun extendedTableHasExactly28PointsAndSeparateTrims() {
        assertEquals((67..94).toList(), SpeedCurveConfiguration.pointCvs(true))
        assertEquals((67..94).toList() + listOf(66, 95), SpeedCurveConfiguration.dataCvs(true))
        assertEquals(31, SpeedCurveConfiguration.readCvs(true).distinct().size)
    }

    @Test
    fun selectingCurvePreservesEveryOtherCv29BitForAllByteValues() {
        for (value in 0..255) {
            val simple = SpeedCurveConfiguration.withSimpleEnabled(value, true)
            val extended = SpeedCurveConfiguration.withSimpleEnabled(value, false)
            assertEquals(value and 0xEF, simple)
            assertEquals(value or 0x10, extended)
            assertTrue(SpeedCurveConfiguration.simpleEnabled(simple))
            assertFalse(SpeedCurveConfiguration.simpleEnabled(extended))
        }
    }
}
