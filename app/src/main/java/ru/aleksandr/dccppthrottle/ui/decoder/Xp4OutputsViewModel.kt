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

class Xp4OutputsViewModel: ViewModel() {

    private var _loaded = MutableLiveData(__loaded)
    val loaded: LiveData<Boolean> = _loaded

    fun getCvValue(cv: Int): Int {
        val idx = cvNumbers.indexOf(cv)
        return cvValues[idx]
    }

    fun setCvValue(cv: Int, value: Int) {
        val idx = cvNumbers.indexOf(cv)
        cvValues[idx] = value
    }

    fun setLoaded(value: Boolean) {
        if (!value) cvValues.fill(0)
        __loaded = value
        _loaded.postValue(__loaded)
    }

    companion object {
        const val IDX_DIMMING = 0
        const val IDX_FADING = 1
        const val IDX_BLINKING = 2
        const val IDX_NEON = 3
        const val IDX_ESAVING = 4
        const val IDX_FIREBOX = 5
        const val IDX_SMOKE = 6
        const val IDX_COUPLERS = 7
        const val IDX_SERVO = 8

        const val UNIT_5MSEC = 0.005
        const val UNIT_10MSEC = 0.010
        const val UNIT_100MSEC = 0.100
        const val UNIT_200MSEC = 0.200

        val cvNumbers = listOf(
            // Dimming the light and function outputs
            116, 117, 118, 119, 120, 121, 122, 123,
            150, 151, 152, 153, 154, 155, 156, 157,

            // Fade-in/fade-out option for lighting and function outputs
            186, 187,

            // Blinking effects for lighting and function outputs
            109, 110, 111, 112,

            // Switch-on effect for a neon light / fluorescent lamp
            188, 189, 190,

            //Energy-saving lighting effect when switching on light and function outputs
            183, 184, 185,

            // Firebox flicker
            181, 182,

            // Smoke Generator control
            130, 131, 132, 133, 134,

            // Control for electric couplers
            124, 125, 126, 127, 128, 129, 135, 136, 137,

            // Servo control
            166, 167, 168, 160, 161, 162, 163, 164, 165
        )
        private val cvValues = Array(cvNumbers.size) { 0 }
        private var __loaded = false
    }
}