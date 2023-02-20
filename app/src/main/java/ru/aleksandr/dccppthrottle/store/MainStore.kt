package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object MainStore {
    private val _viewPagerPosition: MutableLiveData<Int> = MutableLiveData(0)
    val viewPagerPosition: LiveData<Int> = _viewPagerPosition

    private val _trackPower: MutableLiveData<Boolean> = MutableLiveData(false)
    val trackPower: LiveData<Boolean> = _trackPower

    fun setViewPagerPosition(value: Int) {
        _viewPagerPosition.postValue(value)
    }

    fun setTrackPower(value: Boolean) {
        _trackPower.postValue(value)
    }
}