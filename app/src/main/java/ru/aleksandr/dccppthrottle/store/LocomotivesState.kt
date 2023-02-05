package ru.aleksandr.dccppthrottle.store

import kotlin.random.Random

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object LocomotivesState {

    /**
     * An array of sample (placeholder) items.
     */
//    val ITEMS: MutableList<LocomotiveSlot> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
//    val ITEM_MAP: MutableMap<Number, LocomotiveSlot> = HashMap()

    const val SLOTS_COUNT = 10
    const val FUNCTIONS_COUNT = 12

    val SLOTS = Array(SLOTS_COUNT) {
        LocomotiveSlot(it + 1, 0)
    }

    init {
//        // Add some sample items.
//        for (i in 1..SLOTS_COUNT) {
//            addItem(createPlaceholderItem(i))
//        }
        for (i in 0 until SLOTS_COUNT) {
            if (Random.nextBoolean()) {
                val item = SLOTS[i]
                item.address = (1..255).random()
                item.speed = (0..100).random()
                for (k in 0 until FUNCTIONS_COUNT) {
                    item.f[k] = Random.nextBoolean()
                }
            }
        }
    }

//    private fun addItem(item: LocomotiveSlot) {
//        ITEMS.add(item)
//        ITEM_MAP.put(item.slot, item)
//    }
//
//    private fun createPlaceholderItem(position: Int): LocomotiveSlot {
//        val addr = (1..255).random()
//        val speed = (-100..100).random()
//        val item = LocomotiveSlot(position, addr, speed)
//        item.functions[1] = true
//        return LocomotiveSlot(position, addr, speed)
//    }

    /**
     * A placeholder item representing a piece of content.
     */
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