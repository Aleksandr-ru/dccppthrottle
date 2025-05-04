/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import ru.aleksandr.dccppthrottle.ui.decoder.CvListModel

class Xp4SettingsViewModel: CvListModel(
    // Conf
    29, 50, 51,
) {
    companion object {
        const val IDX_CONF = 0
    }
}