/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.ArrayDeque

object ConsoleStore {
    private const val MAX_LINES = 256
    const val TAG_IN = "IN"
    const val TAG_OUT = "OUT"

    private val lines = ArrayDeque<Line>()
    private var nextId = 0L
    private val _data = MutableLiveData<List<Line>>(emptyList())
    val data: LiveData<List<Line>> = _data

    fun addIn(str: String) {
        add(str, TAG_IN)
    }

    fun addOut(str: String) {
        add(str, TAG_OUT)
    }

    private fun add(str: String, tag: String) = synchronized(lines) {
        lines.addLast(Line(str, tag, nextId++))
        if (lines.size > MAX_LINES) lines.removeFirst()
        // DiffUtil needs snapshots that cannot change while it compares them.
        _data.postValue(lines.toList())
    }

    fun clear() = synchronized(lines) {
        lines.clear()
        _data.postValue(emptyList())
    }

    data class Line(val str: String, val tag: String, val id: Long) {
        override fun toString() = "$tag $str"
    }
}
