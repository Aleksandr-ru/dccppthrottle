/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

class Xp5SettingsViewModel: CvListModel(
    // Conf
    29, 47, 62,

    // Fading
    177, 178,

    // Flashing
    173, 174, 175, 176,

    // fluorescent lamp, Energy-saving lamp
    172, 170, 171,

    // servo
    202, 203, 204,
    208, 209, 210,
    214, 215, 216,
    220, 221, 222,

    // switching off
    180, 181, 182, 183, 184, 185, 186, 187, 188,

    // coupling
    130, 135,
    131, 132, 133, 134,
) {
    companion object {
        const val IDX_CONF = 0
        const val IDX_FADING = 1
        const val IDX_FLASHING = 2
        const val IDX_LAMPS = 3
        const val IDX_SERVO = 4
        const val IDX_SWOFF = 5
        const val IDX_COUPLING = 6

        const val UNIT_20MSEC = 0.020
        const val UNIT_100MSEC = 0.100
        const val UNIT_05SEC = 0.500
    }
}