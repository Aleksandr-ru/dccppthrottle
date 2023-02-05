package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object AccessoriesStore {
    private val _data = MutableLiveData<MutableList<AccessoryState>>(mutableListOf())
    val data: LiveData<MutableList<AccessoryState>> = _data

    fun add(item: AccessoryState) {
        _data.value = _data.value?.also {
            it.add(item)
        }
    }

    fun removeByIndex(index: Int) {
        _data.value = _data.value?.also {
            it.filterIndexed { i, item -> i != index }
        }
    }

    fun replaceByIndex(index: Int, newItem: AccessoryState) {
        _data.value = _data.value?.also {
            it.set(index, newItem)
        }
    }

    fun setStateByIndex(index: Int, newState: Boolean) {
        _data.value = _data.value?.also {
            it.mapIndexed { i, item ->
                item.takeIf { i == index }?.apply {
                    state = newState
                }
            }
        }
    }

    fun setStateByAddress(address: Int, newState: Boolean) {
        _data.value = _data.value?.also {
            it.map { item ->
                item.takeIf { it.address == address }?.apply {
                    state = newState
                }
            }
        }
    }

    data class AccessoryState(
        var address: Int,
        var title: String? = null
    ) {
        var state: Boolean = false

        override fun toString(): String = title ?: ("Untitled accessory $address")
    }
}