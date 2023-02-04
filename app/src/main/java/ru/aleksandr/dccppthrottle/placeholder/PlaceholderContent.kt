package ru.aleksandr.dccppthrottle.placeholder

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<PlaceholderItem> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<Number, PlaceholderItem> = HashMap()

    private val COUNT = 10

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createPlaceholderItem(i))
        }
    }

    private fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.slot, item)
    }

    private fun createPlaceholderItem(position: Int): PlaceholderItem {
        val addr = (1..255).random()
        val speed = (-100..100).random()
        return PlaceholderItem(position, addr, speed)
    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(val slot: Number, val addr: Number, val speed: Number = 0, val name: String? = null) {
        override fun toString(): String = name ?: ("DCC:$addr (default)")
    }
}