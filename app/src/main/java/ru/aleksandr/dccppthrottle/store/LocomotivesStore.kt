package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

object LocomotivesStore {
    private const val SLOTS_COUNT = 10
    const val FUNCTIONS_COUNT = 12

    private val _data = MutableLiveData<MutableList<LocomotiveState>>(mutableListOf())
    val data : LiveData<MutableList<LocomotiveState>> = _data

//    val slots : LiveData<List<Int>> = data.map {
//        it.map { item -> item.slot }.filter { i -> i > 0 }
//    }

    fun getSlots() = data.value?.map { it.slot }!!.filter { it > 0 }

    fun liveSlot(slot : Int) : LiveData<LocomotiveState> = data.map { it.first { item -> item.slot == slot } }

    fun getAvailableSlot() : Int {
        val slots = getSlots()
        for (i in 1..SLOTS_COUNT) {
            if (slots.indexOf(i) == -1) return i
        }
        return 0
    }

    fun hasFreeSlots() : Boolean = getSlots().size < SLOTS_COUNT

//    fun getTakenSlots() = data.value!!.filter { it.address > 0 } ?: listOf<LocomotiveState>()

    fun getSlotByIndex(index : Int) : Int = data.value!![index].slot
    fun getSlotByAddress(addr : Int) : Int = data.value!!.first { it.address == addr }?.slot ?: 0

    fun add(item: LocomotiveState) {
        _data.value = _data.value?.also {
            it.add(item)
        }
    }

    fun assignToSlot(index: Int, newSlot: Int) {
        _data.value = _data.value?.also {
            it.mapIndexed { i, item ->
                item.takeIf { i == index }?.apply {
                    slot = newSlot
                }
            }
        }
    }

    data class LocomotiveState(
        var address: Int,
        var title: String? = null
    ) {
        val f = BooleanArray(FUNCTIONS_COUNT)
        var speed: Int = 0
        var reverse: Boolean = false
        var slot: Int = 0

        override fun toString(): String = title ?: ("DCC:$address (untitled)")
    }
}