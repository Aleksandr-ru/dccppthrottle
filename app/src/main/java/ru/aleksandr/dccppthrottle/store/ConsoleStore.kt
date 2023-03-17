/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object ConsoleStore {
    private const val MAX_LINES = 256
    const val TAG_IN = "IN"
    const val TAG_OUT = "OUT"

    private val _data = MutableLiveData<MutableList<Line>>(mutableListOf())
    val data : LiveData<MutableList<Line>> = _data

    fun addIn(str: String) {
        _data.postValue(_data.value?.also {
            it.add(Line(str, TAG_IN))
            if (it.size > MAX_LINES) it.removeAt(0)
        })
    }

    fun addOut(str: String) {
        _data.postValue(_data.value?.also {
            it.add(Line(str, TAG_OUT))
            if (it.size > MAX_LINES) it.removeAt(0)
        })
    }

    fun clear() {
        _data.postValue(mutableListOf())
    }

    data class Line(val str: String, val tag: String) {
        override fun toString() = "$tag $str"
    }
}