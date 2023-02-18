package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object MainStore {
    private val _position: MutableLiveData<Int> = MutableLiveData(0)
    val position: LiveData<Int> = _position

    fun setPosition(value: Int) {
        _position.postValue(value)
    }
}