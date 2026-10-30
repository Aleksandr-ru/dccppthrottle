package ru.aleksandr.dccppthrottle.cs

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class BluetoothLineBufferTest {
    @Test
    fun partialAndMultipleLinesAreDeliveredOnlyWhenComplete() {
        val buffer = BluetoothLineBuffer(1024)
        val messages = mutableListOf<String>()
        fun receive(chunk: String) {
            val bytes = chunk.toByteArray(Charsets.UTF_8)
            buffer.append(bytes, bytes.size) { messages.add(it) }
        }

        receive("<p")
        assertEquals(emptyList<String>(), messages)
        receive("1>\n<T 1 0 1>\n<r")
        assertEquals(listOf("<p1>", "<T 1 0 1>"), messages)
        receive(" 8 151>\n")
        assertEquals(listOf("<p1>", "<T 1 0 1>", "<r 8 151>"), messages)
    }

    @Test
    fun onlyReturnedBytesAreConsumedAndCrLfIsAccepted() {
        val buffer = BluetoothLineBuffer(1024)
        val messages = mutableListOf<String>()
        val bytes = "<p0>\r\nunused\n".toByteArray()
        buffer.append(bytes, 6) { messages.add(it) }
        buffer.append(bytes, 0) { messages.add(it) }
        assertEquals(listOf("<p0>"), messages)
    }

    @Test
    fun messageAtSizeLimitIsAcceptedAndBufferIsReused() {
        val buffer = BluetoothLineBuffer(4)
        val messages = mutableListOf<String>()
        val bytes = "1234\n5678\n".toByteArray()
        buffer.append(bytes, bytes.size) { messages.add(it) }
        assertEquals(listOf("1234", "5678"), messages)
    }

    @Test(expected = IOException::class)
    fun oversizedMessageFailsEvenWhenSplitAcrossReads() {
        val buffer = BluetoothLineBuffer(4)
        buffer.append("1234".toByteArray(), 4) { }
        buffer.append("5".toByteArray(), 1) { }
    }

    @Test
    fun utf8CharacterSplitAcrossReadsIsPreserved() {
        val buffer = BluetoothLineBuffer(1024)
        val messages = mutableListOf<String>()
        val bytes = "станция\n".toByteArray(Charsets.UTF_8)
        buffer.append(bytes.copyOfRange(0, 1), 1) { messages.add(it) }
        buffer.append(bytes.copyOfRange(1, bytes.size), bytes.size - 1) { messages.add(it) }
        assertEquals(listOf("станция"), messages)
    }
}
