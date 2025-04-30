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

class Lp5SettingsViewModel: ViewModel() {

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
        const val IDX_CONF = 0
        const val IDX_COUPLERS = 1

        const val UNIT_16MSEC = 0.016

        val cvNumbers = listOf(
            // Conf
            29, 49, 124,

            // Couplers
            246, 247, 248,
        )
        private val cvValues = Array(cvNumbers.size) { 0 }
        private var __loaded = false
    }
}