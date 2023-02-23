package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

object RoutesStore {
    const val SORT_UNSORTED = "unsorted"
    const val SORT_NAME = "name"

    private var sort_order = "unsorted"

    private val _data = MutableLiveData<MutableList<RouteState>>(mutableListOf())
    val data: LiveData<MutableList<RouteState>> = _data

    fun liveAccessories(routeIndex: Int) = data.map { it[routeIndex].accessories }

    fun add(item: RouteState) {
        _data.postValue(sorted(_data.value!!.also {
            it.add(item)
        }))
    }

    fun addAccessory(routeIndex: Int, acc: RouteStateAccessory) {
        val route = data.value!![routeIndex]
        route.accessories.add(acc)
        replace(routeIndex, route)
    }

    fun remove(index: Int) {
        _data.postValue(_data.value?.also {
            it.removeAt(index)
        })
    }

    fun removeAccessory(routeIndex: Int, accIndex: Int) {
        val route = data.value!![routeIndex]
        route.accessories.removeAt(accIndex)
        replace(routeIndex, route)
    }

    fun replace(index: Int, newItem: RouteState) {
        _data.postValue(sorted(_data.value!!.also {
            it[index] = newItem
        }))
    }

    fun setTitle(index: Int, newTitle: String) {
        _data.postValue(sorted(_data.value!!.also {
            it[index].title = newTitle
        }))
    }

    fun replaceAccessory(routeIndex: Int, accIndex: Int, newItem: RouteStateAccessory) {
        _data.postValue(_data.value?.also {
            it[routeIndex].accessories[accIndex] = newItem
        })
    }

    fun toggleAccessory(routeIndex: Int, accIndex: Int, isOn: Boolean) {
        _data.postValue(_data.value?.also {
            it[routeIndex].accessories[accIndex].isOn = isOn
        })
    }

    private fun sorted(list: MutableList<RouteState>) : MutableList<RouteState> {
        return when (sort_order) {
            "name" -> {
                list.sortedWith(compareBy { it.title })
            }
            else -> {
                list
            }
        }.toMutableList()
    }

    fun sort(order: String) {
        sort_order = order
        _data.postValue(sorted(_data.value!!))
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