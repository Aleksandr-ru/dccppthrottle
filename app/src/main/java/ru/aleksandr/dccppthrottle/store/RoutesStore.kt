package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

object RoutesStore {
    private val _data = MutableLiveData<MutableList<RouteState>>(mutableListOf())
    val data: LiveData<MutableList<RouteState>> = _data

    fun liveAccessories(routeIndex: Int) = data.map { it[routeIndex].accessories }

    fun add(item: RouteState) {
        _data.value = _data.value?.also {
            it.add(item)
        }
    }

    fun addAccessory(routeIndex: Int, acc: RouteStateAccessory) {
        val route = data.value!![routeIndex]
        route.accessories.add(acc)
        replace(routeIndex, route)
    }

    fun remove(index: Int) {
        _data.value = _data.value?.also {
            it.removeAt(index)
        }
    }

    fun removeAccessory(routeIndex: Int, accIndex: Int) {
        val route = data.value!![routeIndex]
        route.accessories.removeAt(accIndex)
        replace(routeIndex, route)
    }

    fun replace(index: Int, newItem: RouteState) {
        _data.value = _data.value?.also {
            it[index] = newItem
        }
    }

    fun setTitle(index: Int, newTitle: String) {
        _data.value = _data.value?.also {
            it[index].title = newTitle
        }
    }

    fun replaceAccessory(routeIndex: Int, accIndex: Int, newItem: RouteStateAccessory) {
        _data.value = _data.value?.also {
            it[routeIndex].accessories[accIndex] = newItem
        }
    }

    fun toggleAccessory(routeIndex: Int, accIndex: Int, isOn: Boolean) {
        _data.value = _data.value?.also {
            it[routeIndex].accessories[accIndex].isOn = isOn
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