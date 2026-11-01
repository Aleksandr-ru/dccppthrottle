/*
 * Copyright (c) 2026. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.cs

internal class CvReadCommand(cv: Int, id: Long) {
    // Both echoed callback fields fit in a signed 16-bit integer.
    private val callbackNum = (id / 32767 % 32767 + 1).toInt()
    private val callbackSub = (id % 32767).toInt()
    val command = "<R $cv $callbackNum $callbackSub>"
    val response = Regex("""<r$callbackNum\|$callbackSub\|($cv) (-?\d+)>""")
}
