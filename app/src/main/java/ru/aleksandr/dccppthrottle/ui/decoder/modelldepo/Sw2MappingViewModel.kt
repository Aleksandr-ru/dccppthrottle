/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.modelldepo

import ru.aleksandr.dccppthrottle.ui.decoder.CvListModel

val cvs = (165 .. 224).toList().toIntArray()

class Sw2MappingViewModel: CvListModel(*cvs) {}