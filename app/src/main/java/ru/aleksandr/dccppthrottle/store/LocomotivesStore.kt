/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import org.json.JSONArray
import org.json.JSONObject

object LocomotivesStore : JsonStoreInterface {
    const val SLOTS_COUNT = 12
    const val FUNCTIONS_COUNT = 29

    const val SORT_UNSORTED = "unsorted"
    const val SORT_NAME = "name"
    const val SORT_ADDR = "address"
    const val SORT_SLOT_NAME = "slot_name"
    const val SORT_SLOT_ADDR = "slot_addr"

    private var _sortOrder = SORT_UNSORTED
    private val _data = MutableLiveData<MutableList<LocomotiveState>>(mutableListOf())
    val data : LiveData<MutableList<LocomotiveState>> = _data

    override var hasUnsavedData = false

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
    fun getIndexBySlot(slot: Int) : Int = data.value!!.indexOfFirst { it.slot == slot }

    fun getBySlot(slot: Int) : LocomotiveState? = data.value?.firstOrNull { it.slot == slot }
    fun getByAddress(addr: Int) : LocomotiveState? = data.value?.firstOrNull { it.slot > 0 && it.address == addr }

    fun add(item: LocomotiveState) {
        if (item.slot > 0 && getSlots().indexOf(item.slot) > -1) {
            throw LocomotiveSlotInUseException()
        }
        _data.postValue(sort(_data.value!!.also {
            it.add(item)
        }))
        hasUnsavedData = true
    }

    fun remove(index: Int) {
        _data.postValue(_data.value?.also {
            it.removeAt(index)
        })
        hasUnsavedData = true
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
        _data.postValue(sort(_data.value!!.also {
            it[index] = newItem
        }))
        hasUnsavedData = true
    }

    fun assignToSlot(index: Int, toSlot: Int? = null) : Int {
        val newSlot = toSlot ?: getAvailableSlot() ?: throw LocomotiveNoSlotsAvailableException()
        if (newSlot > 0) {
            val addr = getAddress(index)
            if (getSlotByAddress(addr) > 0) throw LocomotiveAddressInUseException()
        }
        _data.postValue(sort(_data.value!!.also {
            it.mapIndexed { i, item ->
                item.takeIf { i == index }?.apply {
                    slot = newSlot
                }
            }
        }))
        hasUnsavedData = true
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

    fun setSpeedByAddress(addr: Int, newSpeed: Int, newReverse: Boolean) {
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.slot > 0 && it.address == addr }?.apply {
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

    fun stopByAddress(addr: Int) {
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.slot > 0 && it.address == addr }?.apply {
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

    fun setFunctionByAddress(addr: Int, func: Int, isOn : Boolean = false) {
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.slot > 0 && it.address == addr }?.apply {
                    functions[func] = isOn
                }
            }
        })
    }

    fun setAllFuncBySlot(slot: Int, allFunc: BooleanArray) {
        if (allFunc.size != FUNCTIONS_COUNT) {
            throw LocomotiveIncorrectFunctionsException()
        }
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.slot == slot }?.apply {
                    for (i in 0 until FUNCTIONS_COUNT) {
                        functions[i] = allFunc[i]
                    }
                }
            }
        })
    }

    fun setAllFuncByAddress(addr: Int, allFunc: BooleanArray) {
        if (allFunc.size != FUNCTIONS_COUNT) {
            throw LocomotiveIncorrectFunctionsException()
        }
        _data.postValue(_data.value?.also {
            it.map { item ->
                item.takeIf { it.slot >0 && it.address == addr }?.apply {
                    for (i in 0 until FUNCTIONS_COUNT) {
                        functions[i] = allFunc[i]
                    }
                }
            }
        })
    }

    private fun sort(list: MutableList<LocomotiveState>) : MutableList<LocomotiveState> {
        return when (_sortOrder) {
            SORT_NAME -> {
                list.sortedWith(compareBy { it.title })
            }
            SORT_ADDR -> {
                list.sortedWith(compareBy { it.address })
            }
            SORT_SLOT_NAME -> {
                list.sortedWith(compareBy({ it.slot == 0 }, { it.slot }, { it.title }))
            }
            SORT_SLOT_ADDR -> {
                list.sortedWith(compareBy({ it.slot == 0 }, { it.slot }, { it.address }))
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
        val list = mutableListOf<LocomotiveState>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val item = LocomotiveState(
                jsonObject.getInt("address"),
                jsonObject.optString("title").ifEmpty { null },
                jsonObject.optInt("minSpeed", 1),
                jsonObject.optInt("maxSpeed", 100)
            ).apply {
                slot = jsonObject.getInt("slot")
                val names = jsonObject.optJSONArray("funcNames")
                if (names != null) {
                    (0 until names.length()).forEach {
                        funcNames[it] = names.getString(it)
                    }
                }
                else {
                    // backwards compatibility
                    funcNames[0] = "Lights"
                }
            }
            list.add(item)
        }
        _data.postValue(sort(list))
        hasUnsavedData = false
    }

    data class LocomotiveState(
        var address: Int,
        var title: String? = null,
        var minSpeed: Int = 1,
        var maxSpeed: Int = 100
    ) {
        val functions = BooleanArray(FUNCTIONS_COUNT)
        val funcNames = Array<String>(FUNCTIONS_COUNT) { "" }
        var speed: Int = 0
        var reverse: Boolean = false
        var slot: Int = 0

        override fun toString(): String = title ?: ("DCC:$address (untitled)")

        fun toJson() = JSONObject().apply {
            put("address", address)
            if (title != null) put("title", title)
            put("minSpeed", minSpeed)
            put("maxSpeed", maxSpeed)
            put("slot", slot)
            put("funcNames", JSONArray(funcNames))
        }
    }

    open class LocomotiveStoreException() : Exception() {}
    class LocomotiveNoSlotsAvailableException() : LocomotiveStoreException() {}
    class LocomotiveNoSlotAddressException() : LocomotiveStoreException() {}
    class LocomotiveAddressInUseException() : LocomotiveStoreException() {}
    class LocomotiveSlotInUseException() : LocomotiveStoreException() {}
    class LocomotiveIncorrectFunctionsException() : LocomotiveStoreException() {}
}