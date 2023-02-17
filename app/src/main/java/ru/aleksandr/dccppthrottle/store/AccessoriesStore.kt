package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object AccessoriesStore {
    private val _data = MutableLiveData<MutableList<AccessoryState>>(mutableListOf())
    val data: LiveData<MutableList<AccessoryState>> = _data

    fun add(item: AccessoryState) {
        if (_data.value?.any { it.address == item.address } == true) {
            throw AccessoryAddressInUseException()
        }
        _data.value = _data.value?.also {
            it.add(item)
        }
    }

    fun getByAddress(addr: Int) = data.value?.find { it.address == addr }

    fun getAddress(index: Int) = data.value?.get(index)?.address
    fun getIndexByAddress(addr: Int) = data.value?.withIndex()?.find { it.value.address == addr }?.index

    fun remove(index: Int) {
        _data.value = _data.value?.also {
            it.removeAt(index)
        }
    }

    fun replace(index: Int, newItem: AccessoryState) {
        if (_data.value?.withIndex()?.filter { it.index != index }?.any { it.value.address == newItem.address } == true) {
            throw AccessoryAddressInUseException()
        }
        _data.value = _data.value?.also {
            it[index] = newItem
        }
    }

    fun hasAddress(addr: Int) : Boolean = data.value!!.any { it.address == addr }

    fun setState(index: Int, newState: Boolean) {
        _data.value = _data.value?.also {
            it.mapIndexed { i, item ->
                item.takeIf { i == index }?.apply {
                    isOn = newState
                }
            }
        }
    }

    fun setStateByAddress(address: Int, newState: Boolean) {
        _data.value = _data.value?.also {
            it.map { item ->
                item.takeIf { it.address == address }?.apply {
                    isOn = newState
                }
            }
        }
    }

    data class AccessoryState(
        var address: Int,
        var title: String? = null
    ) {
        var isOn: Boolean = false

        override fun toString(): String = title ?: ("Untitled accessory $address")
    }

    open class AccessoriesStoreException() : Exception() {}
    class AccessoryAddressInUseException() : AccessoriesStoreException() {}
}