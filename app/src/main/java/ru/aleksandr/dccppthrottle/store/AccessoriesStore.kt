package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import org.json.JSONObject

object AccessoriesStore {
    const val SORT_UNSORTED = "unsorted"
    const val SORT_NAME = "name"
    const val SORT_ADDR = "address"

    private var sort_order = SORT_UNSORTED

    private val _data = MutableLiveData<MutableList<AccessoryState>>(mutableListOf())
    val data: LiveData<MutableList<AccessoryState>> = _data

    fun add(item: AccessoryState) {
        if (_data.value?.any { it.address == item.address } == true) {
            throw AccessoryAddressInUseException()
        }
        _data.postValue(sorted(_data.value!!.also {
            it.add(item)
        }))
    }

    fun getByAddress(addr: Int) = data.value?.find { it.address == addr }

    fun getAddress(index: Int) = data.value?.get(index)?.address
    fun getIndexByAddress(addr: Int) =
        data.value?.withIndex()?.find { it.value.address == addr }?.index

    fun remove(index: Int) {
        _data.postValue(_data.value?.also {
            it.removeAt(index)
        })
    }

    fun replace(index: Int, newItem: AccessoryState) {
        if (_data.value?.withIndex()?.filter { it.index != index }
                ?.any { it.value.address == newItem.address } == true) {
            throw AccessoryAddressInUseException()
        }
        _data.postValue(sorted(_data.value!!.also {
            it[index] = newItem
        }))
    }

    fun hasAddress(addr: Int, skipIndex: Int = -1): Boolean =
        data.value!!.withIndex().any { it.index != skipIndex && it.value.address == addr }

    fun setState(index: Int, newState: Boolean) {
        _data.postValue(_data.value?.also {
            it.mapIndexed { i, item ->
                item.takeIf { i == index }?.apply {
                    isOn = newState
                }
            }
        })
    }

    fun setStateByAddress(address: Int, newState: Boolean) {
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.address == address }?.apply {
                    isOn = newState
                }
            }
        })
    }

    private fun sorted(list: MutableList<AccessoryState>): MutableList<AccessoryState> {
        return when (sort_order) {
            SORT_NAME -> {
                list.sortedWith(compareBy { it.title })
            }
            SORT_ADDR -> {
                list.sortedWith(compareBy { it.address })
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

    fun toJson() = JSONArray(_data.value!!.map { it.toJson() })

    fun fromJson(jsonArray: JSONArray) {
        val list = mutableListOf<AccessoryState>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val item = AccessoryState(
                jsonObject.getInt("address"),
                jsonObject.optString("title", null)
            ).apply {
                isOn = jsonObject.getBoolean("isOn")
            }
            list.add(item)
        }
        _data.postValue(sorted(list))
    }

    data class AccessoryState(
        var address: Int,
        var title: String? = null
    ) {
        var isOn: Boolean = false

        override fun toString(): String = title ?: ("Untitled accessory $address")

        fun toJson() = JSONObject().apply {
            put("address", address)
            if (title != null) put("title", title)
            put("isOn", isOn)
        }
    }

    open class AccessoriesStoreException() : Exception() {}
    class AccessoryAddressInUseException() : AccessoriesStoreException() {}
}