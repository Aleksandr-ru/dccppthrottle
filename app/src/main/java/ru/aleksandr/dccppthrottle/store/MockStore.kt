package ru.aleksandr.dccppthrottle.store

import kotlin.random.Random

object MockStore {
    private val btDevices = listOf<String>(
        "HC-06-115200",
        "HC-05",
        "Breezer 4S",
        "Mac-book-Air",
        "Realme 8 Pro",
        "SPS 705"
    )

    private val locomotives = listOf<String>(
        "BR-80 (Piko)",
        "BR120 (Piko)",
        "GP 35 (Bachmann)",
        "Breuer traktor (Rivarossi)",
        "Hercules (Piko new)",
        "V 200 (Piko new)",
        "Y-2126 (Ree)",
        "BR 78 (Piko new)"
    )

    private val accessories = listOf<String>(
        "Turnout",
        "Signal"
    )

    fun ramdomBluetoothList() : List<String> = btDevices.shuffled()

    fun randomLocomotive() : LocomotivesStore.LocomotiveState {
        val addr = (1..255).random()
        return LocomotivesStore.LocomotiveState(addr).apply {
            speed = (0..100).random()
            if (Random.nextBoolean()) {
                title = locomotives.random()
            }
            for (k in 0 until LocomotivesStore.FUNCTIONS_COUNT) {
                functions[k] = Random.nextBoolean()
            }
        }
    }

    fun randomAccessory() : AccessoriesStore.AccessoryState {
        val addr = (1..255).random()
        return AccessoriesStore.AccessoryState(addr).apply {
            if (Random.nextBoolean()) {
                title = accessories.random() + " " + (1..99).random()
            }
        }
    }
}