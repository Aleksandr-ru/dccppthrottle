/*
 * Copyright (c) 2026. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.cs

internal sealed class StationStateMessage {
    data class Power(val on: Boolean, val joined: Boolean) : StationStateMessage()
    data class Throttle(val slot: Int, val speed: Int, val reverse: Boolean) : StationStateMessage()
    data class Functions(val address: Int, val mask: Int) : StationStateMessage()
    data class Current(val current: Int, val max: Int, val trip: Int) : StationStateMessage()
}

/** Compiled once; state notifications do not depend on pending requests. */
internal object StationMessageParser {
    private val power = Regex("<p(0|1)( JOIN)?>")
    private val throttle = Regex("""<T (\d+) (-?\d+) (0|1)>""")
    private val functions = Regex("""<l (\d+) (\d+) (\d+) (\d+)>""")
    private val current = Regex("""<c CurrentMAIN (\d+) C Milli (\d) (\d+) (0|1) (\d+)>""")

    fun parse(message: String): StationStateMessage? {
        power.matchEntire(message)?.groupValues?.let {
            return StationStateMessage.Power(it[1] == "1", it[2].isNotEmpty())
        }
        throttle.matchEntire(message)?.groupValues?.let {
            return StationStateMessage.Throttle(
                it[1].toIntOrNull() ?: return null,
                it[2].toIntOrNull() ?: return null,
                it[3] == "0"
            )
        }
        functions.matchEntire(message)?.groupValues?.let {
            return StationStateMessage.Functions(
                it[1].toIntOrNull() ?: return null,
                it[4].toIntOrNull() ?: return null
            )
        }
        current.matchEntire(message)?.groupValues?.let {
            return StationStateMessage.Current(
                it[1].toIntOrNull() ?: return null,
                it[3].toIntOrNull() ?: return null,
                it[5].toIntOrNull() ?: return null
            )
        }
        return null
    }
}
