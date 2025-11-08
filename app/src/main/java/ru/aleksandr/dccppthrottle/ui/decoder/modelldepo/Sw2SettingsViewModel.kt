/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.modelldepo

import ru.aleksandr.dccppthrottle.ui.decoder.CvListModel

class Sw2SettingsViewModel: CvListModel(
    // Conf
    29,

    // Modes
    *modeCvs.flatten().toIntArray()
) {
    companion object {
        val modeCvs = listOf(
            listOf(94,  95,  96,  98,  99,  100),
            listOf(105, 106, 107, 109, 110, 111),
            listOf(120, 121, 122, 124, 125, 126),
            listOf(135, 136, 137, 139, 140, 141),
            listOf(150, 151, 152, 154, 155, 156)
        )

        const val UNIT_420MSEC = 0.420
    }
}