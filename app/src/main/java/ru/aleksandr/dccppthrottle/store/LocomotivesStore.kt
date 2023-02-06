package ru.aleksandr.dccppthrottle.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.random.Random

object LocomotivesStore {
    private const val SLOTS_COUNT = 10
    const val FUNCTIONS_COUNT = 12

    private val _data = MutableLiveData<Array<LocomotiveSlot>>(Array(SLOTS_COUNT) {
        LocomotiveSlot(it + 1, 0)
    })
    val data : LiveData<Array<LocomotiveSlot>> = _data

    init {
        _data.value = _data.value?.also {
            it.map { item ->
//                when (Random.nextBoolean()) {
//                    true -> MockStore.randomLocomotive(item.slot)
//                    else -> item
//                }
                MockStore.randomLocomotive(item.slot)
            }
        }
    }

    fun getTakenSlots() = data.value?.filter { it.address > 0 } ?: listOf<LocomotiveSlot>()

    data class LocomotiveSlot(
        val slot: Int,
        var address: Int,
        var title: String? = null
    ) {
        val f = BooleanArray(FUNCTIONS_COUNT)
        var speed: Int = 0
        var minSpeed: Int = 1
        var maxSpeed: Int = 100
        var reverse: Boolean = false

        override fun toString(): String = title ?: ("DCC:$address (untitled)")
    }
}