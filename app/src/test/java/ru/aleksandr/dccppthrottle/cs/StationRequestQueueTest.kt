package ru.aleksandr.dccppthrottle.cs

import org.junit.Assert.*
import org.junit.Test

class StationRequestQueueTest {
    private class Timer(val delay: Long, val action: () -> Unit) {
        var cancelled = false
    }

    private class Fixture(stopOnTimeout: Boolean = true, maxPending: Int = 64) {
        val sent = mutableListOf<String>()
        val timers = mutableListOf<Timer>()
        val errors = mutableListOf<Exception>()
        val results = mutableListOf<String>()
        val queue = StationRequestQueue(
            send = { sent.add(it) },
            dispatch = { it() },
            schedule = { delay, action ->
                val timer = Timer(delay, action)
                timers.add(timer)
                val cancel: () -> Unit = { timer.cancelled = true }
                cancel
            },
            stopOnTimeout = stopOnTimeout,
            maxPending = maxPending
        )

        fun submit(name: String, reply: String = name): RequestHandle = queue.submit(
            name, Regex(Regex.escape(reply)), 30000,
            onError = { errors.add(it) }, onResult = { results.add(name) }
        )
    }

    @Test
    fun requestsAreSentOneAtATimeAndCallbacksStayWithTheirRequest() {
        val f = Fixture()
        f.submit("one")
        f.submit("two")
        assertEquals(listOf("one"), f.sent)
        assertEquals(1, f.timers.size)
        f.queue.receive("one")
        assertEquals(listOf("one"), f.results)
        assertEquals(listOf("one", "two"), f.sent)
        assertTrue(f.timers[0].cancelled)
        f.queue.receive("two")
        assertEquals(listOf("one", "two"), f.results)
        assertTrue(f.timers[1].cancelled)
    }

    @Test
    fun oneReplyCompletesOnlyOneRequestWithSamePattern() {
        val f = Fixture()
        f.submit("one", "reply")
        f.submit("two", "reply")
        f.queue.receive("reply")
        assertEquals(listOf("one"), f.results)
        f.queue.receive("reply")
        assertEquals(listOf("one", "two"), f.results)
    }

    @Test
    fun callbackMaySubmitNextRequestWithoutChangingAnIteratedList() {
        val f = Fixture()
        f.queue.submit("one", Regex("one"), 30000, { f.errors.add(it) }) {
            f.submit("two")
        }
        f.queue.receive("one")
        assertEquals(listOf("one", "two"), f.sent)
        f.queue.receive("two")
        assertEquals(listOf("two"), f.results)
    }

    @Test
    fun timeoutFailsActiveAndQueuedRequestsAndBlocksFurtherProgramming() {
        val f = Fixture()
        f.submit("one")
        f.submit("two")
        f.timers[0].action()
        assertTrue(f.errors[0] is RequestTimeoutException)
        assertTrue(f.errors[1] is RequestQueueBlockedException)
        f.submit("three")
        assertTrue(f.errors[2] is RequestQueueBlockedException)
        f.queue.receive("one")
        assertEquals(listOf("one"), f.sent)
        assertTrue(f.results.isEmpty())
    }

    @Test
    fun statusQueueCanContinueAfterTimeout() {
        val f = Fixture(stopOnTimeout = false)
        f.submit("one")
        f.submit("two")
        f.timers[0].action()
        assertEquals(listOf("one", "two"), f.sent)
        f.queue.receive("two")
        assertEquals(listOf("two"), f.results)
    }

    @Test
    fun staleTimerCannotTimeOutNextRequest() {
        val f = Fixture()
        f.submit("one")
        f.submit("two")
        f.queue.receive("one")
        f.timers[0].action()
        assertTrue(f.errors.isEmpty())
        f.queue.receive("two")
        assertEquals(listOf("one", "two"), f.results)
    }

    @Test
    fun disconnectCancelsTimersFailsRequestsAndRejectsFurtherSubmissions() {
        val f = Fixture()
        f.submit("one")
        f.submit("two")
        f.queue.close()
        assertEquals(2, f.errors.size)
        assertTrue(f.errors.all { it is RequestDisconnectedException })
        assertTrue(f.timers.all { it.cancelled })
        f.queue.receive("one")
        f.timers[0].action()
        assertTrue(f.results.isEmpty())
        assertEquals(2, f.errors.size)
        f.submit("three")
        assertEquals(3, f.errors.size)
        assertEquals(listOf("one"), f.sent)
    }

    @Test
    fun cancelledQueuedRequestIsNeverSent() {
        val f = Fixture()
        f.submit("one")
        f.submit("two").cancel()
        f.queue.receive("one")
        assertEquals(listOf("one"), f.sent)
        assertEquals(listOf("one"), f.results)
        assertTrue(f.errors.isEmpty())
    }

    @Test
    fun cancelledActiveRequestIsDrainedBeforeNextOperation() {
        val f = Fixture()
        f.submit("one").cancel()
        f.submit("two")
        assertEquals(listOf("one"), f.sent)
        f.queue.receive("one")
        assertEquals(listOf("one", "two"), f.sent)
        assertTrue(f.results.isEmpty())
        f.queue.receive("two")
        assertEquals(listOf("two"), f.results)
        assertTrue(f.errors.isEmpty())
    }

    @Test
    fun cancelledActiveRequestWithoutReplyStillBlocksOnTimeout() {
        val f = Fixture()
        f.submit("one").cancel()
        f.submit("two")
        f.timers[0].action()
        assertEquals(1, f.errors.size)
        assertTrue(f.errors.single() is RequestQueueBlockedException)
        assertEquals(listOf("one"), f.sent)
    }

    @Test
    fun pendingRequestsHaveAHardLimit() {
        val f = Fixture(maxPending = 2)
        f.submit("one")
        f.submit("two")
        f.submit("three")
        assertTrue(f.errors.single() is RequestQueueFullException)
        f.queue.receive("one")
        f.queue.receive("two")
        assertEquals(listOf("one", "two"), f.sent)
    }

    @Test
    fun unrelatedMessagesDoNotConsumeRequestOrRestartItsTimer() {
        val f = Fixture()
        f.submit("one")
        repeat(100) { f.queue.receive("<p1>") }
        assertTrue(f.results.isEmpty())
        assertEquals(1, f.timers.size)
        assertFalse(f.timers.single().cancelled)
    }

    @Test
    fun staleReadReplyDoesNotCompleteNextReadOfSameCv() {
        val f = Fixture()
        val first = CvReadCommand(8, 0)
        val second = CvReadCommand(8, 1)
        f.queue.submit(first.command, first.response, 30000, { f.errors.add(it) }) {
            f.results.add("first")
        }
        f.queue.submit(second.command, second.response, 30000, { f.errors.add(it) }) {
            f.results.add("second")
        }
        f.queue.receive("<r1|0|8 151>")
        f.queue.receive("<r1|0|8 151>")
        assertEquals(listOf("first"), f.results)
        f.queue.receive("<r1|1|8 151>")
        assertEquals(listOf("first", "second"), f.results)
    }

    @Test
    fun callbackFieldsRollOverWithoutRepeatingAtFirstBoundary() {
        val last = CvReadCommand(8, 32766)
        val next = CvReadCommand(8, 32767)
        assertEquals("<R 8 1 32766>", last.command)
        assertEquals("<R 8 2 0>", next.command)
        assertNull(next.response.matchEntire("<r1|32766|8 151>"))
        assertNotNull(next.response.matchEntire("<r2|0|8 151>"))
    }

    @Test
    fun transportFailureCleansUpTimeoutAndFailsRequest() {
        var cancelled = false
        val errors = mutableListOf<Exception>()
        val queue = StationRequestQueue(
            send = { throw RequestDisconnectedException() }, dispatch = { it() },
            schedule = { _, _ -> { cancelled = true } }, stopOnTimeout = true
        )
        queue.submit("one", Regex("one"), 30000, { errors.add(it) }) { fail("Unexpected result") }
        assertTrue(cancelled)
        assertTrue(errors.single() is RequestDisconnectedException)
    }
}
