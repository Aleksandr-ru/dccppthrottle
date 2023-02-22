package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

object LocomotivesStore {
    const val SLOTS_COUNT = 12
    const val FUNCTIONS_COUNT = 29

    private val _data = MutableLiveData<MutableList<LocomotiveState>>(mutableListOf())
    val data : LiveData<MutableList<LocomotiveState>> = _data

    fun liveSlot(slot : Int) : LiveData<LocomotiveState> = data.map { it.first { item -> item.slot == slot } }

    fun getSlots() : List<Int> = data.value!!.map { it.slot }.filter { it > 0 }

    fun getAvailableSlot() : Int? {
        val slots = getSlots()
        for (i in 1..SLOTS_COUNT) {
            if (slots.indexOf(i) == -1) return i
        }
        return null
    }

    fun hasFreeSlots() : Boolean = getSlots().size < SLOTS_COUNT
    fun getSlot(index : Int) : Int = data.value!![index].slot
    fun getAddress(index: Int) : Int = data.value!![index].address
    fun getSlotByAddress(addr : Int) : Int = data.value!!.firstOrNull { it.address == addr }?.slot ?: 0

    fun getBySlot(slot: Int) : LocomotiveState? = data.value?.firstOrNull { it.slot == slot }

    fun add(item: LocomotiveState) {
        if (item.slot > 0 && getSlots().indexOf(item.slot) > -1) {
            throw LocomotiveSlotInUseException()
        }
        _data.postValue(_data.value?.also {
            it.add(item)
        })
    }

    fun remove(index: Int) {
        _data.postValue(_data.value?.also {
            it.removeAt(index)
        })
    }

    fun replace(index: Int, newItem: LocomotiveState) {
        if (newItem.slot > 0) {
            if (newItem.address == 0) throw LocomotiveNoSlotAddressException()
            val slots : Map<Int, Int> = data.value!!.filterIndexed { i, item -> i != index && item.slot > 0 }
                .map { it.slot to it.address }
                .toMap()
            if (slots.keys.any { it == newItem.slot }) throw LocomotiveSlotInUseException()
            if (slots.values.any { it == newItem.address }) throw LocomotiveAddressInUseException()
        }
        _data.postValue(_data.value?.also {
            it[index] = newItem
        })
    }

    fun assignToSlot(index: Int, toSlot: Int? = null) : Int {
        val newSlot = toSlot ?: getAvailableSlot() ?: throw LocomotiveNoSlotsAvailableException()
        if (newSlot > 0) {
            val addr = getAddress(index)
            if (getSlotByAddress(addr) > 0) throw LocomotiveAddressInUseException()
        }
        _data.postValue(_data.value?.also {
            it.mapIndexed { i, item ->
                item.takeIf { i == index }?.apply {
                    slot = newSlot
                }
            }
        })
        return newSlot
    }

    fun setSpeedBySlot(slot: Int, newSpeed: Int, newReverse: Boolean) {
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.slot == slot }?.apply {
                    speed = newSpeed
                    reverse = newReverse
                }
            }
        })
    }

    fun stopBySlot(slot: Int) {
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.slot == slot }?.apply {
                    speed = 0
                }
            }
        })
    }

    fun setFunction(index: Int, func: Int, isOn : Boolean = false) {
        _data.postValue(_data.value?.also {
            it[index].apply {
                functions[func] = isOn
            }
        })
    }

    fun setFunctionBySlot(slot: Int, func: Int, isOn : Boolean = false) {
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.slot == slot }?.apply {
                    functions[func] = isOn
                }
            }
        })
    }

    data class LocomotiveState(
        var address: Int,
        var title: String? = null
    ) {
        val functions = BooleanArray(FUNCTIONS_COUNT)
        var speed: Int = 0
        var reverse: Boolean = false
        var slot: Int = 0

        override fun toString(): String = title ?: ("DCC:$address (untitled)")
    }

    open class LocomotiveStoreException() : Exception() {}
    class LocomotiveNoSlotsAvailableException() : LocomotiveStoreException() {}
    class LocomotiveNoSlotAddressException() : LocomotiveStoreException() {}
    class LocomotiveAddressInUseException() : LocomotiveStoreException() {}
    class LocomotiveSlotInUseException() : LocomotiveStoreException() {}
}