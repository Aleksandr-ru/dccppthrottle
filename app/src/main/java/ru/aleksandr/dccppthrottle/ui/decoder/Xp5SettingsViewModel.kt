/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map

class Xp5SettingsViewModel: ViewModel() {

    private var _loaded = MutableLiveData(__loaded)
    val loaded: LiveData<Boolean> = _loaded

    private var _cvValues = MutableLiveData(cvValues)

    fun getCvValue(cv: Int): Int {
        val idx = cvNumbers.indexOf(cv)
        return cvValues[idx]
    }

    fun setCvValue(cv: Int, value: Int) {
        val idx = cvNumbers.indexOf(cv)
        val postValue = cvValues[idx] != value
        cvValues[idx] = value
        if (postValue) _cvValues.postValue(cvValues)
    }

    fun setLoaded(value: Boolean) {
        if (!value) cvValues.fill(0)
        __loaded = value
        _loaded.postValue(__loaded)
    }

    fun liveCvValue(cv: Int): LiveData<Int> = _cvValues.map {
        val idx = cvNumbers.indexOf(cv)
        it[idx]
    }.distinctUntilChanged()

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

        val cvNumbers = listOf(
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
        )
        private val cvValues = Array(cvNumbers.size) { 0 }
        private var __loaded = false
    }
}