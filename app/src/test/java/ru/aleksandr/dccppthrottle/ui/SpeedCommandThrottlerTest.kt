package ru.aleksandr.dccppthrottle.ui

import org.junit.Assert.*
import org.junit.Test
import ru.aleksandr.dccppthrottle.ui.cab.SpeedCommandThrottler

class SpeedCommandThrottlerTest {
    private class Timer(val at: Long, val action: () -> Unit) {
        var cancelled = false
        var fired = false
    }

    private class Fixture {
        var time = 0L
        var valid = true
        val timers = mutableListOf<Timer>()
        val sent = mutableListOf<Pair<Long, Int>>()
        val sender = SpeedCommandThrottler(
            now = { time },
            schedule = { delay, action ->
                val timer = Timer(time + delay, action)
                timers.add(timer)
                val cancel: () -> Unit = { timer.cancelled = true }
                cancel
            },
            send = { sent.add(time to it) },
            isValid = { valid }
        )

        fun advanceTo(target: Long) {
            while (true) {
                val next = timers.filter { !it.cancelled && !it.fired && it.at <= target }
                    .minByOrNull { it.at } ?: break
                time = next.at
                next.fired = true
                next.action()
            }
            time = target
        }
    }

    @Test
    fun continuousDragSendsLatestSpeedEvery250MsWithoutWaitingForPause() {
        val f = Fixture()
        f.sender.submit(10)
        f.advanceTo(50)
        f.sender.submit(20)
        f.advanceTo(150)
        f.sender.submit(30)
        f.advanceTo(249)
        assertEquals(listOf(0L to 10), f.sent)
        f.advanceTo(250)
        assertEquals(listOf(0L to 10, 250L to 30), f.sent)
        f.advanceTo(260)
        f.sender.submit(40)
        f.advanceTo(450)
        f.sender.submit(50)
        f.advanceTo(500)
        assertEquals(listOf(0L to 10, 250L to 30, 500L to 50), f.sent)
    }

    @Test
    fun releaseFlushesFinalSpeedImmediatelyAndCancelsPendingTimer() {
        val f = Fixture()
        f.sender.submit(10)
        f.advanceTo(100)
        f.sender.submit(20)
        f.advanceTo(120)
        f.sender.flush(25)
        f.advanceTo(1000)
        assertEquals(listOf(0L to 10, 120L to 25), f.sent)
        assertTrue(f.timers.all { it.cancelled })
    }

    @Test
    fun identicalSpeedsAndReleaseDoNotSendDuplicates() {
        val f = Fixture()
        f.sender.submit(10)
        repeat(20) { f.sender.submit(10) }
        f.sender.flush(10)
        assertEquals(listOf(0L to 10), f.sent)
        assertTrue(f.timers.isEmpty())
    }

    @Test
    fun returningToLastSentSpeedDiscardsIntermediatePendingValue() {
        val f = Fixture()
        f.sender.submit(10)
        f.advanceTo(50)
        f.sender.submit(20)
        f.advanceTo(80)
        f.sender.submit(10)
        f.advanceTo(1000)
        assertEquals(listOf(0L to 10), f.sent)
    }

    @Test
    fun zeroSpeedIsSentImmediatelyWithoutWaitingForInterval() {
        val f = Fixture()
        f.sender.submit(50)
        f.advanceTo(50)
        f.sender.submit(40)
        f.advanceTo(60)
        f.sender.submit(0)
        f.advanceTo(1000)
        assertEquals(listOf(0L to 50, 60L to 0), f.sent)
    }

    @Test
    fun cancelPreventsScheduledSpeedIncludingAlreadyQueuedStaleCallback() {
        val f = Fixture()
        f.sender.submit(10)
        f.advanceTo(50)
        f.sender.submit(20)
        val oldTimer = f.timers.single()
        f.sender.cancel()
        f.advanceTo(1000)
        oldTimer.action()
        assertEquals(listOf(0L to 10), f.sent)
    }

    @Test
    fun externalStopOrChangedLocomotiveInvalidatesPendingSpeedAndRelease() {
        val f = Fixture()
        f.sender.submit(10)
        f.advanceTo(50)
        f.sender.submit(20)
        f.valid = false
        f.advanceTo(250)
        f.sender.flush(30)
        f.sender.submit(40)
        f.advanceTo(1000)
        assertEquals(listOf(0L to 10), f.sent)
    }

    @Test
    fun stationarySliderDoesNotKeepSchedulingTimers() {
        val f = Fixture()
        f.sender.submit(10)
        f.advanceTo(50)
        f.sender.submit(20)
        f.advanceTo(5000)
        assertEquals(1, f.timers.size)
        assertEquals(listOf(0L to 10, 250L to 20), f.sent)
    }

    @Test
    fun separateCabsHaveIndependentTimersAndCancellation() {
        val left = Fixture()
        val right = Fixture()
        left.sender.submit(10)
        right.sender.submit(60)
        left.advanceTo(50)
        right.advanceTo(50)
        left.sender.submit(20)
        right.sender.submit(70)
        left.sender.cancel()
        left.advanceTo(250)
        right.advanceTo(250)
        assertEquals(listOf(0L to 10), left.sent)
        assertEquals(listOf(0L to 60, 250L to 70), right.sent)
    }

    @Test
    fun idleGapAllowsImmediateNextSpeed() {
        val f = Fixture()
        f.sender.submit(10)
        f.advanceTo(1000)
        f.sender.submit(20)
        assertEquals(listOf(0L to 10, 1000L to 20), f.sent)
        assertTrue(f.timers.isEmpty())
    }
}
