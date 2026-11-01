/*
 * Copyright (c) 2026. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.cs

import java.io.IOException

/** Retains partial lines between reads, with a fixed limit on message size. */
internal class BluetoothLineBuffer(maxLineBytes: Int) {
    private val line = ByteArray(maxLineBytes)
    private var size = 0

    fun append(bytes: ByteArray, count: Int, onLine: (String) -> Unit) {
        for (index in 0 until count) {
            val byte = bytes[index]
            if (byte == '\n'.code.toByte()) {
                val length = if (size > 0 && line[size - 1] == '\r'.code.toByte()) size - 1 else size
                val message = String(line, 0, length, Charsets.UTF_8)
                size = 0
                onLine(message)
            } else {
                if (size == line.size) throw IOException("Bluetooth message exceeds ${line.size} bytes")
                line[size++] = byte
            }
        }
    }
}
