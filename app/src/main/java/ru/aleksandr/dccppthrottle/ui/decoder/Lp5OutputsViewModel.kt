/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import androidx.lifecycle.ViewModel

class Lp5OutputsViewModel: ViewModel() {
    companion object {
        private val cvNumbers = listOf(
            // Mode Select CV, Switching-On/-Off Delay, Automatic Switch Off, Brightness CV, Special Function CV1, Special Function CV2, Special Function CV3
            listOf(259, 260, 261, 262, 263, 264, 258), // Light front (Config. 1)
            listOf(267, 268, 269, 270, 271, 273, 266), // Light back (Config. 1)
            listOf(275, 276, 277, 278, 279, 280, 274), // AUX1 (Config. 1)
            listOf(283, 284, 285, 286, 287, 288, 282), // AUX2 (Config. 1) 
            listOf(291, 292, 293, 294, 295, 296, 290), // AUX3
            listOf(299, 300, 301, 302, 303, 304, 298), // AUX4
            listOf(307, 308, 309, 310, 311, 312, 306), // AUX5
            listOf(315, 316, 317, 318, 319, 320, 314), // AUX6
            listOf(323, 324, 325, 326, 327, 328, 322), // AUX7
            listOf(331, 332, 333, 334, 335, 336, 330), // AUX8
            listOf(339, 340, 341, 342, 343, 344, 338), // AUX9
            listOf(347, 348, 349, 350, 351, 352, 346), // AUX10
            listOf(355, 356, 357, 358, 359, 360, 354), // AUX11
            listOf(363, 364, 365, 366, 367, 368, 362), // AUX12
            listOf(371, 372, 373, 374, 375, 376, 370), // AUX13
            listOf(379, 380, 381, 382, 383, 384, 378), // AUX14
            listOf(387, 388, 389, 390, 391, 392, 386), // AUX15
            listOf(395, 396, 397, 398, 399, 400, 394), // AUX16
            listOf(403, 404, 405, 406, 407, 408, 402), // AUX17
            listOf(411, 412, 413, 414, 415, 416, 410), // AUX18
            listOf(419, 420, 421, 422, 423, 424, 418), // Light front (Config. 2)
            listOf(427, 428, 429, 430, 431, 432, 426), // Light back (Config. 2)
            listOf(435, 436, 437, 438, 439, 440, 434), // AUX1 (Config. 2)
            listOf(443, 444, 445, 446, 447, 448, 442), // AUX2 (Config. 2)
        )
    }
}