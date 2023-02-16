package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Collections

object RoutesStore {
    private val _data = MutableLiveData<MutableList<RouteState>>(mutableListOf())
    val data: LiveData<MutableList<RouteState>> = _data

    fun add(item: RouteState) {
        _data.value = _data.value?.also {
            it.add(item)
        }
    }

    fun addAccessory(routeIndex: Int, acc: RouteStateAccessory) {
        val route = data.value!![routeIndex]
        route.accessories.add(acc)
        replaceByIndex(routeIndex, route)
    }

    fun removeByIndex(index: Int) {
        _data.value = _data.value?.also {
            it.removeAt(index)
        }
    }

    fun removeAccessoryByIndex(routeIndex: Int, accIndex: Int) {
        val route = data.value!![routeIndex]
        route.accessories.removeAt(accIndex)
        replaceByIndex(routeIndex, route)
    }

    fun replaceByIndex(index: Int, newItem: RouteState) {
        _data.value = _data.value?.also {
            it[index] = newItem
        }
    }

    data class RouteState(
        var title : String
    ) {
        var accessories : MutableList<RouteStateAccessory> = mutableListOf()

        override fun toString() = title
    }

    data class RouteStateAccessory(
        var address : Int,
        var delay : Int = 0
    ) {
        var isOn : Boolean = false

        override fun toString() = AccessoriesStore.getByAddress(address).toString()
    }
}