/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.modelldepo

import android.content.Context
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.ui.decoder.CvListModel

class Sw2OutputsViewModel: CvListModel(
    *(30 .. 42).toList().toIntArray(),
    *(46 .. 58).toList().toIntArray(),
    *(62 .. 74).toList().toIntArray(),
    *(78 .. 90).toList().toIntArray()
) {
    fun getEffectsMap(context: Context): Map<Int, String> {
        val stringArray = context.resources.getStringArray(R.array.sw2_output_effects)
        val regex = Regex("""^(\d+): (.+)$""")
        return stringArray.mapNotNull { str ->
            regex.matchEntire(str)?.let {
                it.groupValues[1].toInt() to it.groupValues[2]
            }
        }.toMap()
    }
}