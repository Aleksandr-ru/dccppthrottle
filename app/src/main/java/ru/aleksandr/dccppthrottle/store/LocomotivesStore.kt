package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

object LocomotivesStore {
    private const val SLOTS_COUNT = 10
    const val FUNCTIONS_COUNT = 24

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
    fun getSlotByIndex(index : Int) : Int = data.value?.get(index)?.slot ?: throw IndexOutOfBoundsException()
    fun getAddressByIndex(index: Int) : Int = data.value?.get(index)?.address ?: throw IndexOutOfBoundsException()
    fun getSlotByAddress(addr : Int) : Int = data.value?.first { it.address == addr }?.slot ?: 0

    fun add(item: LocomotiveState) {
        if (item.slot > 0 && getSlots().indexOf(item.slot) > -1) {
            throw LocomotiveSlotInUseException()
        }
        _data.value = _data.value?.also {
            it.add(item)
        }
    }

    fun removeByIndex(index: Int) {
        _data.value = _data.value?.also {
            it.removeAt(index)
        }
    }

    fun replaceByIndex(index: Int, newItem: LocomotiveState) {
        if (newItem.slot > 0) {

        }
        _data.value = _data.value?.also {
            it[index] = newItem
        }
    }

    fun assignToSlotByIndex(index: Int, toSlot: Int? = null) {
        val newSlot = toSlot ?: getAvailableSlot() ?: throw LocomotiveNoSlotsAvailableException()
        if (newSlot > 0) {
            val addr = getAddressByIndex(index)
            if (getSlotByAddress(addr) > 0) throw LocomotiveAddressInUseException()
        }
        _data.value = _data.value?.also {
            it.mapIndexed { i, item ->
                item.takeIf { i == index }?.apply {
                    slot = newSlot
                }
            }
        }
    }

    fun setFunctionByIndex(index: Int, func: Int, isOn : Boolean = false) {
        _data.value = _data.value?.also {
            it[index].apply {
                functions[func] = isOn
            }
        }
    }

    fun setFunctionBySlot(slot: Int, func: Int, isOn : Boolean = false) {
        _data.value = _data.value?.also {
            it.map { item ->
                item.takeIf { it.slot == slot }?.apply {
                    functions[func] = isOn
                }
            }
        }
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
    class LocomotiveAddressInUseException() : LocomotiveStoreException() {}
    class LocomotiveSlotInUseException() : LocomotiveStoreException() {}
}