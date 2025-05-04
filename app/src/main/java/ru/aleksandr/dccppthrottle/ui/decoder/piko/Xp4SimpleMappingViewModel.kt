/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import ru.aleksandr.dccppthrottle.ui.decoder.CvListModel

class Xp4SimpleMappingViewModel: CvListModel(
    // simple function mapping
    96,

    // function mapping
    33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46,

    // shift
    100, 101,

    // Activating front and rear train lighting
    107, 108,

    // Switch off function outputs depending on the direction of travel
    113, 114,
) {
    companion object {
        val shiftMap = listOf(
            Pair(100, 4), // 33
            Pair(100, 5), // 34
            Pair(100, 0), // 35
            Pair(100, 1), // 36
            Pair(100, 2), // 37
            Pair(100, 3), // 38
            Pair(101, 0), // 39
            Pair(101, 1), // 40
            Pair(101, 2), // 41
            Pair(101, 3), // 42
            Pair(101, 4), // 43
            Pair(101, 5), // 44
            Pair(101, 6), // 45
            Pair(101, 7), // 46
        )
    }
}