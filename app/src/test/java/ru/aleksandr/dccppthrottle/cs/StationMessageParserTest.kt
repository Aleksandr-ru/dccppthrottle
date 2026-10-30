package ru.aleksandr.dccppthrottle.cs

import org.junit.Assert.*
import org.junit.Test

class StationMessageParserTest {
    @Test
    fun powerNotificationsAreParsedWithoutPendingCommand() {
        assertEquals(StationStateMessage.Power(true, true), StationMessageParser.parse("<p1 JOIN>"))
        assertEquals(StationStateMessage.Power(false, false), StationMessageParser.parse("<p0>"))
    }

    @Test
    fun throttleNotificationsIdentifySlotAndEmergencyStop() {
        assertEquals(StationStateMessage.Throttle(3, 126, false), StationMessageParser.parse("<T 3 126 1>"))
        assertEquals(StationStateMessage.Throttle(3, -1, true), StationMessageParser.parse("<T 3 -1 0>"))
    }

    @Test
    fun functionNotificationsKeepAddressAndFullF0ToF28Mask() {
        assertEquals(StationStateMessage.Functions(123, 536870911),
            StationMessageParser.parse("<l 123 0 128 536870911>"))
    }

    @Test
    fun currentNotificationExtractsCurrentMaxAndTrip() {
        assertEquals(StationStateMessage.Current(50, 2000, 1500),
            StationMessageParser.parse("<c CurrentMAIN 50 C Milli 1 2000 0 1500>"))
    }

    @Test
    fun requestRepliesAndUnknownMessagesAreLeftToRequestQueues() {
        listOf("<iDCC-EX>", "<r1|0|8 151>", "<r 8 -1>", "<X>", "garbage").forEach {
            assertNull(StationMessageParser.parse(it))
        }
    }

    @Test
    fun outOfRangeNumbersDoNotCrashMessageProcessing() {
        assertNull(StationMessageParser.parse("<T 1 99999999999999999 1>"))
        assertNull(StationMessageParser.parse("<l 123 0 128 99999999999999999>"))
    }
}
