/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.prog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProgViewModel : ViewModel() {
    private val _cvNum: MutableLiveData<Int> = MutableLiveData()
    val cvNum: LiveData<Int> = _cvNum

    private val _cvVal: MutableLiveData<Int> = MutableLiveData()
    val cvVal: LiveData<Int> = _cvVal

    fun setCvNum(value: Int) {
        _cvNum.postValue(value)
        if (valuesMap.containsKey(value)) {
            _cvVal.postValue(valuesMap[value]!!)
        }
    }

    fun setCvVal(value: Int) {
        _cvVal.postValue(value)
    }

    fun storeValue(cv: Int, value: Int) {
        valuesMap[cv] = value
    }

    companion object {
        private val valuesMap: MutableMap<Int, Int> = mutableMapOf()
    }
}