/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object MainStore {
    // https://stackoverflow.com/questions/2220560/can-an-android-toast-be-longer-than-toast-length-long/5079536#5079536
    const val LONG_DELAY = 3500L
    const val SHORT_DELAY = 2000L

    private val _viewPagerPosition: MutableLiveData<Int> = MutableLiveData(0)
    val viewPagerPosition: LiveData<Int> = _viewPagerPosition

    private val _trackPower: MutableLiveData<Boolean> = MutableLiveData(false)
    val trackPower: LiveData<Boolean> = _trackPower

    private val _trackCurrent: MutableLiveData<Map<String, Int>> = MutableLiveData(mapOf())
    val trackCurrent: LiveData<Map<String, Int>> = _trackCurrent

    fun setViewPagerPosition(value: Int) {
        _viewPagerPosition.postValue(value)
    }

    fun setTrackPower(value: Boolean) {
        _trackPower.postValue(value)
    }

    fun setTrackCurrent(value: Map<String, Int>) {
        _trackCurrent.postValue(value)
    }
}