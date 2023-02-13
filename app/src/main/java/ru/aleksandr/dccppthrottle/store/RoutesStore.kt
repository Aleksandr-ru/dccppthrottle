package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object RoutesStore {
    private val _data = MutableLiveData<MutableList<RouteState>>(mutableListOf())
    val data: LiveData<MutableList<RouteState>> = _data

    fun add(item: RouteState) {
        _data.value = _data.value?.also {
            it.add(item)
        }
    }

    fun removeByIndex(index: Int) {
        _data.value = _data.value?.also {
            it.removeAt(index)
        }
    }

    fun replaceByIndex(index: Int, newItem: RouteState) {
        _data.value = _data.value?.also {
            it[index] = newItem
        }
    }

    data class RouteState(
        var title : String
    ) {
        var accessories : MutableList<AccessoriesStore.AccessoryState> = mutableListOf()

        override fun toString() = title
    }
}