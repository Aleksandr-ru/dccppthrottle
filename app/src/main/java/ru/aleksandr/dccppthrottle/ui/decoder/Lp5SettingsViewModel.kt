/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

class Lp5SettingsViewModel: CvListModel(
    // Conf
    29, 49, 124, 101,

    // Couplers
    246, 247, 248,
) {
    companion object {
        const val IDX_CONF = 0
        const val IDX_COUPLERS = 1

        const val UNIT_16MSEC = 0.016
    }
}