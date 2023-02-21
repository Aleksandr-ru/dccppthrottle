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
    }

    fun setCvVal(value: Int) {
        _cvVal.postValue(value)
    }
}