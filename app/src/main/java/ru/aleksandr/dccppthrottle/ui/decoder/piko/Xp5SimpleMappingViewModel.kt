/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import ru.aleksandr.dccppthrottle.ui.decoder.CvListModel

class Xp5SimpleMappingViewModel: CvListModel(
    // simple function mapping
    96,

    // function mapping
    33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46,

    // witch off function outputs depending on the direction of travel
    97, 98,
) {}