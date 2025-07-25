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

    private val _mainViewPagerPosition: MutableLiveData<Int> = MutableLiveData(0)
    val mainViewPagerPosition: LiveData<Int> = _mainViewPagerPosition

    private val _cabViewPagerPosition: MutableLiveData<Int> = MutableLiveData(0)
    val cabViewPagerPosition: LiveData<Int> = _cabViewPagerPosition

    private val _dualCabViewPagerPosition: MutableLiveData<Pair<Int, Int>> = MutableLiveData(Pair(0, 1))
    val dualCabViewPagerPosition: LiveData<Pair<Int, Int>> = _dualCabViewPagerPosition

    private val _trackPower: MutableLiveData<Pair<Boolean, Boolean>> = MutableLiveData(Pair(false, false))
    val trackPower: LiveData<Pair<Boolean, Boolean>> = _trackPower

    private val _trackCurrent: MutableLiveData<Map<String, Int>> = MutableLiveData(mapOf())
    val trackCurrent: LiveData<Map<String, Int>> = _trackCurrent

    fun setMainViewPagerPosition(value: Int) {
        _mainViewPagerPosition.postValue(value)
    }

    fun setCabViewPagerPosition(value: Int) {
        _cabViewPagerPosition.postValue(value)
    }

    fun setDualCabPositionLeft(value: Int) {
        _dualCabViewPagerPosition.postValue(
            Pair(value, _dualCabViewPagerPosition.value!!.second)
        )
    }

    fun setDualCabPositionRight(value: Int) {
        _dualCabViewPagerPosition.postValue(
            Pair(_dualCabViewPagerPosition.value!!.first, value)
        )
    }

    fun setTrackPower(power: Boolean, join: Boolean) {
        _trackPower.postValue(Pair(power, join))
    }

    fun setTrackCurrent(value: Map<String, Int>) {
        _trackCurrent.postValue(value)
    }
}