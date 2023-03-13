package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import org.json.JSONArray
import org.json.JSONObject

object RoutesStore : JsonStoreInterface {
    const val SORT_UNSORTED = "unsorted"
    const val SORT_NAME = "name"

    private var _sortOrder = SORT_UNSORTED
    private val _data = MutableLiveData<MutableList<RouteState>>(mutableListOf())
    val data: LiveData<MutableList<RouteState>> = _data

    override var hasUnsavedData = false

    fun liveAccessories(routeIndex: Int) = data.map { it[routeIndex].accessories }

    fun add(item: RouteState) {
        _data.postValue(sort(_data.value!!.also {
            it.add(item)
        }))
        hasUnsavedData = true
    }

    fun addAccessory(routeIndex: Int, acc: RouteStateAccessory) {
        val route = data.value!![routeIndex]
        route.accessories.add(acc)
        replace(routeIndex, route)
        hasUnsavedData = true
    }

    fun remove(index: Int) {
        _data.postValue(_data.value?.also {
            it.removeAt(index)
        })
        hasUnsavedData = true
    }

    fun removeAccessory(routeIndex: Int, accIndex: Int) {
        val route = data.value!![routeIndex]
        route.accessories.removeAt(accIndex)
        replace(routeIndex, route)
        hasUnsavedData = true
    }

    fun removeAccFromAll(addr: Int) : Int {
        var countRemoved = 0
        _data.postValue(_data.value!!.also {
            it.map { route ->
                if (route.accessories.removeAll { it.address == addr })
                    countRemoved++
                route
            }
        })
        hasUnsavedData = true
        return countRemoved
    }

    fun replace(index: Int, newItem: RouteState) {
        _data.postValue(sort(_data.value!!.also {
            it[index] = newItem
        }))
        hasUnsavedData = true
    }

    fun setTitle(index: Int, newTitle: String) {
        _data.postValue(sort(_data.value!!.also {
            it[index].title = newTitle
        }))
        hasUnsavedData = true
    }

    fun replaceAccessory(routeIndex: Int, accIndex: Int, newItem: RouteStateAccessory) {
        _data.postValue(_data.value?.also {
            it[routeIndex].accessories[accIndex] = newItem
        })
        hasUnsavedData = true
    }

    fun toggleAccessory(routeIndex: Int, accIndex: Int, isOn: Boolean) {
        _data.postValue(_data.value?.also {
            it[routeIndex].accessories[accIndex].isOn = isOn
        })
        hasUnsavedData = true
    }

    private fun sort(list: MutableList<RouteState>) : MutableList<RouteState> {
        return when (_sortOrder) {
            SORT_NAME -> {
                list.sortedWith(compareBy { it.title })
            }
            else -> {
                list
            }
        }.toMutableList()
    }

    fun setSortOrder(order: String) {
        _sortOrder = order
        _data.postValue(sort(_data.value!!))
    }

    override fun toJson() = JSONArray(_data.value!!.map { it.toJson() })

    override fun fromJson(jsonArray: JSONArray, sortOrder: String?) {
        sortOrder?.let {
            _sortOrder = it
        }
        val list = mutableListOf<RouteState>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val accList = mutableListOf<RouteStateAccessory>()
            val accJsonArray : JSONArray = jsonObject.getJSONArray("accessories")
            for (k in 0 until accJsonArray.length()) {
                val accJsonObject = accJsonArray.getJSONObject(k)
                val accItem = RouteStateAccessory(
                    accJsonObject.getInt("address"),
                    accJsonObject.getInt("delay")
                ).apply {
                    isOn = accJsonObject.getBoolean("isOn")
                }
                accList.add(accItem)
            }

            val item = RouteState(
                jsonObject.getString("title")
            ).apply {
                accessories = accList
            }
            list.add(item)
        }
        _data.postValue(sort(list))
        hasUnsavedData = false
    }

    data class RouteState(
        var title : String
    ) {
        var accessories : MutableList<RouteStateAccessory> = mutableListOf()

        override fun toString() = title

        fun toJson() = JSONObject().apply {
            put("title", title)
            put("accessories", JSONArray(accessories.map { it.toJson() }))
        }
    }

    data class RouteStateAccessory(
        var address : Int,
        var delay : Int = 0
    ) {
        var isOn : Boolean = false

        override fun toString() = AccessoriesStore.getByAddress(address).toString()

        fun toJson() = JSONObject().apply {
            put("address", address)
            put("delay", delay)
            put("isOn", isOn)
        }
    }
}