/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.store

import kotlin.random.Random

object MockStore {
    private val btDevices = listOf(
        "HC-06-115200",
        "HC-05",
        "Breezer 4S",
        "Mac-book-Air",
        "Realme 8 Pro",
        "SPS 705"
    )

    private val locomotives = listOf(
        "BR-80 (old Piko)",
        "BR120 (old Piko)",
        "GP 35 (Bachmann)",
        "Breuer traktor (Rivarossi)",
        "Hercules (Piko new)",
        "V 200 (Piko new)",
        "Y-2126 (Ree)",
        "BR 78 (Piko new)",
        "D.141 (Piko new)"
    )

    private val funcs = listOf(
        "Cabin light",
        "Interior light",
        "Horn",
        "Shunting mode"
    )

    private val accessories = listOf(
        "Turnout",
        "Signal",
        "Servo"
    )

    private val routes = listOf(
        "Freight shed",
        "Station platform",
        "Middle track",
        "Main track transit",
        "Tunnel to deport",
        "Diagonal track",
        "Bridge to factory"
    )

    fun ramdomBluetoothList() : List<String> = btDevices.shuffled()

    fun randomLocomotive(randFunc: Boolean = true) : LocomotivesStore.LocomotiveState {
        val addr = (1..255).random()
        return LocomotivesStore.LocomotiveState(addr).apply {
            speed = (0..100).random()
            reverse = Random.nextBoolean()
            if (Random.nextBoolean()) {
                title = locomotives.random()
            }
            if (randFunc) for (k in 0 until LocomotivesStore.FUNCTIONS_COUNT) {
                functions[k] = Random.nextBoolean()
            }
            funcNames[0] = "Lights"
            if (Random.nextBoolean()) {
                funcNames[(1..9).random()] = funcs.random()
            }
        }
    }

    fun randomAccessory() : AccessoriesStore.AccessoryState {
        val addr = (1..255).random()
        val delay = (0 .. 15).random() * 100
        return AccessoriesStore.AccessoryState(addr).apply {
            if (Random.nextBoolean()) {
                title = accessories.random() + " " + (1..99).random()
            }
            this.delay = delay
        }
    }

    fun randomRoute() : RoutesStore.RouteState {
        val title = routes.random()
        return RoutesStore.RouteState(title)
    }

    fun randomRouteAccessory(): RoutesStore.RouteStateAccessory {
        val addr = AccessoriesStore.data.value!!.random().address
        return RoutesStore.RouteStateAccessory(addr)
    }

    fun randomLp5ControlCvValue(): Int {
        return if ((1..10).random() > 8) {
            1 shl ((0..7).random())
        } else 0
    }

    fun randomLp5OutputCvValue(): Int {
        return (0 .. 31).random()
    }
}