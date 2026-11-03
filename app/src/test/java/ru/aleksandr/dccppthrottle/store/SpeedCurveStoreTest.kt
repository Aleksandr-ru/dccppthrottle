package ru.aleksandr.dccppthrottle.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedCurveStoreTest {
    @Test
    fun simplePointsFollowStartMidMaximumOrder() {
        assertEquals(listOf(2, 6, 5), SpeedCurveStore.pointCvs(false))
        assertEquals(listOf(29, 2, 6, 5), SpeedCurveStore.readCvs(false))
    }

    @Test
    fun extendedTableHasExactly28PointsAndSeparateTrims() {
        assertEquals((67..94).toList(), SpeedCurveStore.pointCvs(true))
        assertEquals((67..94).toList() + listOf(66, 95), SpeedCurveStore.dataCvs(true))
        assertEquals(31, SpeedCurveStore.readCvs(true).distinct().size)
    }

    @Test
    fun selectingCurvePreservesEveryOtherCv29BitForAllByteValues() {
        for (value in 0..255) {
            val simple = SpeedCurveStore.withSimpleEnabled(value, true)
            val extended = SpeedCurveStore.withSimpleEnabled(value, false)
            assertEquals(value and 0xEF, simple)
            assertEquals(value or 0x10, extended)
            assertTrue(SpeedCurveStore.simpleEnabled(simple))
            assertFalse(SpeedCurveStore.simpleEnabled(extended))
        }
    }
}
