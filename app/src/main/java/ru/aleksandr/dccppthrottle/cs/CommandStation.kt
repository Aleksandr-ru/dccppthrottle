/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.cs

import android.util.Log
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.ConsoleStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.MainStore
import kotlin.math.min
import kotlin.math.roundToInt

// https://github.com/DCC-EX/BaseStation-Classic/blob/master/DCCpp/SerialCommand.cpp#L82
// https://dcc-ex.com/reference/software/command-reference.html
// https://dcc-ex.com/throttles/tech-reference.html

object CommandStation {
    private const val EMERGENCY_STOP = -1
    private const val MAX_SPEED_STEPS = 126

    private val TAG = javaClass.simpleName

    private var connection: BluetoothConnection? = null
    private var deviceName: String? = null

    private var resultListenersList: ArrayList<Command> = arrayListOf()
    private var writeCvProgCallback: ((cv: Int, value: Int) -> Unit)? = null
    private var readCvProgCallback: ((cv: Int, value: Int) -> Unit)? = null
    private var statusCallback: ((status: String) -> Unit)? = null

    //region Public methods

    fun isConnected() = connection != null
    fun getAddress() = connection?.getAddress()
    fun getName() = deviceName

    fun setConnection(conn: BluetoothConnection, name: String) {
        connection = conn
        deviceName = name
        connection!!.setOnReceiveListener {
            ConsoleStore.addIn(it)
            parseMessage(it)
        }
    }

    fun disconnect() {
        connection?.apply {
            setOnReceiveListener(null)
            close()
        }
        connection = null
        deviceName = null
        ConsoleStore.clear()
    }

    fun getStatus(callback: ((status: String) -> Unit)? = null) {
        statusCallback = callback
        val command = StatusCommand()
        sendCommand(command)
    }

    fun getTrackCurrent() {
        val command = CurrentCommand()
        sendCommand(command)
    }

    fun setTrackPower(isOn: Boolean, join: Boolean = false) {
        val power = if (isOn) 1 else 0
        val command = PowerCommand(power, join)
        sendCommand(command)
    }

    fun unassignAll() {
        val command = UnassignAllCommand()
        sendCommand(command)
    }

    fun unassignLoco(slot: Int) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val command = UnassignCommand(it.address)
            sendCommand(command)
        }
    }

    fun setLocomotiveSpeed(slot: Int, speed: Int, reverse: Boolean? = null) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val direction = (reverse ?: it.reverse).let { rev ->
                if (rev) 0 else 1
            }
            val speedSteps = percentToSpeedSteps(speed)
            val command = ThrottleCommand(it.slot, it.address, speedSteps, direction)
            sendCommand(command)
        }
    }

    fun stopLocomotive(slot: Int) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val direction = if (it.reverse) 0 else 1
            val command = ThrottleCommand(it.slot, it.address, EMERGENCY_STOP, direction)
            sendCommand(command)
        }
    }

    fun emergencyStop() {
        val command = EmergencyCommand()
        sendCommand(command)

        for (i in LocomotivesStore.getSlots())
            LocomotivesStore.stopBySlot(i)
    }

    fun setLocomotiveFunction(slot: Int, func: Int, isOn: Boolean) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val active = if (isOn) 1 else 0
            val command = FunctionCommand(it.address, func, active)
            sendCommand(command)
        }
    }

    fun setAccessoryState(address: Int, isOn: Boolean) {
        val activate = if (isOn) 1 else 0
        val command = AccessoryCommand(address, activate)
        sendCommand(command)
        AccessoriesStore.setStateByAddress(address, isOn)
    }
    // todo set accessory state with subaddress
    // val command = AccessoryCommand(address, 0, activate)

    fun setCvMain(slot: Int, cv: Int, value: Int) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val command = WriteCvMainCommand(it.address, cv, value)
            sendCommand(command)
        }
    }

    fun setCvBitMain(slot: Int, cv: Int, bit: Int, value: Int) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val command = WriteCvMainCommand(it.address, cv, bit, value)
            sendCommand(command)
        }
    }

    fun setCvProg(cv: Int, value: Int, callback: ((cv: Int, value: Int) -> Unit)? = null) {
        writeCvProgCallback = callback
        val command = WriteCvProgCommand(cv, value)
        sendCommand(command)
    }

    fun getCvProg(cv: Int, callback: ((cv: Int, value: Int) -> Unit)? = null) {
        readCvProgCallback = callback
        val command = ReadCvProgCommand(cv)
        sendCommand(command)
    }

    fun setSpeedSteps(speedSteps: String) {
        if (!arrayOf("SPEED28", "SPEED128").contains(speedSteps)) {
            throw CommandStationException()
        }
        val command = SpeedDCommand(speedSteps)
        sendCommand(command)
    }

    //endregion

    //region Private methods

    private fun sendCommand(command: Command) {
        val str = command.toString()
        connection?.let {
            if (command.resultRegex != null) {
                resultListenersList.removeAll { it.processed }
                resultListenersList.add(command)
            }
            it.send(str)
            ConsoleStore.addOut(str)
        }
    }

    private fun parseMessage(mes: String) {
        // todo parse errors <*Too much locos

        resultListenersList.forEach { com ->
            if (!com.processed) Regex(com.resultRegex!!).matchEntire(mes)?.let {
                com.resultListener(it.groupValues)
                com.processed = true // to avoid ConcurrentModificationException
                return@forEach
            }
        }
    }

    private fun percentToSpeedSteps(percent: Int) : Int {
        return if (percent > 0) {
            val speedSteps = percent.toFloat() * MAX_SPEED_STEPS.toFloat() / 100F
            min(MAX_SPEED_STEPS.toFloat(), speedSteps).roundToInt()
        }
        else 0
    }

    private fun speedStepsToPercent(speedSteps: Int) : Int {
        return if (speedSteps > 0) {
            val percent = speedSteps.toFloat() * 100F / MAX_SPEED_STEPS.toFloat()
            min(100F, percent).roundToInt()
        }
        else 0
    }

    //endregion

    //region Command Station commands

    private abstract class Command {
        var processed = false
        abstract val resultRegex: String?
        abstract fun resultListener(groupValues: List<String>)
        abstract override fun toString(): String
    }

    private class StatusCommand() : Command() {
        override val resultRegex = "<i(.+)>"
        override fun resultListener(groupValues: List<String>) {
            // <iDCC-EX V-3.0.4 / MEGA / STANDARD_MOTOR_SHIELD G-75ab2ab>
            if (BuildConfig.DEBUG) Log.i(TAG, groupValues[1])
            statusCallback?.invoke(groupValues[1])
        }
        override fun toString() = "<s>"
    }

    private class PowerCommand(val p: Int, val join: Boolean = false) : Command() {
        override val resultRegex = "<p(0|1)( JOIN)?>"
        override fun resultListener(groupValues: List<String>) {
            val power = groupValues[1].toInt() > 0
            val joined = groupValues[2].isNotEmpty()
            MainStore.setTrackPower(power)
            MainStore.setTrackJoin(joined)
        }
        override fun toString() = if (join && p > 0) "<$p JOIN>" else "<$p>"
    }

    private class CurrentCommand() : Command() {
        override val resultRegex = """<c CurrentMAIN (\d+) C Milli (\d) (\d+) (0|1) (\d+)>"""
        override fun resultListener(groupValues: List<String>) {
            val currentMa = groupValues[1].toInt()
            val maxMa = groupValues[3].toInt()
            val tripMa = groupValues[5].toInt()
            if (BuildConfig.DEBUG) Log.i(TAG, String.format("Current: %d mA, max: %d mA, trip: %d mA", currentMa, maxMa, tripMa))
            MainStore.setTrackCurrent(mapOf("current" to currentMa, "max" to maxMa, "trip" to tripMa))
        }
        override fun toString() = "<c>"
    }

    private class UnassignCommand(val address: Int) : Command() {
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
        override fun toString() = "<- $address>"
    }

    private class UnassignAllCommand() : Command() {
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
        override fun toString() = "<->"
    }

    // Emergency Stop ALL TRAINS. (But leaves power to the track turned on)
    private class EmergencyCommand() : Command() {
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
        override fun toString() = "<!>"
    }

    private class ThrottleCommand(
        val register: Int,
        val cab: Int,
        val speed: Int,
        val direction: Int
    ) : Command() {
        override val resultRegex = """<T ($register) (-?\d+) (0|1)>"""
        override fun resultListener(groupValues: List<String>) {
            val slot = groupValues[1].toInt()
            val speedSteps = groupValues[2].toInt()
            val speedPercent = speedStepsToPercent(speedSteps)
            val reverse = groupValues[3].toInt() == 0
            if (BuildConfig.DEBUG) Log.i(TAG, "Slot: $slot, speed $speedSteps ($speedPercent%), reverse $reverse")
            LocomotivesStore.setSpeedBySlot(register, speedPercent, reverse)
            //TODO: <l result
        }
        override fun toString() = "<t $register $cab $speed $direction>"
    }

    private class FunctionCommand(
        val cab: Int,
        val func: Int,
        val activate: Int
    ) : Command() {
        override val resultRegex = """<l ($cab) (\d+) (\d+) (\d+)>"""
        override fun resultListener(groupValues: List<String>) {
            val addr = groupValues[1].toInt()
            val slot = groupValues[2].toInt() + 1
            val speedDir = groupValues[3].toInt()
            val speed = speedDir and 0b01111111
            val direction = speedDir.shr(7)
            val func = groupValues[4].toInt() // 536870911 max
            val funcStr = func.toString(2).padStart(LocomotivesStore.FUNCTIONS_COUNT, '0')
            val funcArr = funcStr.map { it == '1' }.reversed().toBooleanArray()
            if (BuildConfig.DEBUG) {
                val logArr = funcArr.mapIndexed{ index, b -> if (b) index else -1 }.filter { it > -1 }
                Log.i(TAG, "Cab $addr, slot: $slot, speed $speed, direction $direction, functions $logArr")
            }
//            LocomotivesStore.setSpeedBySlot(slot, speedStepsToPercent(speed), direction == 0)
//            LocomotivesStore.setAllFuncBySlot(slot, funcArr)
            LocomotivesStore.setAllFuncByAddress(addr, funcArr)
        }
        override fun toString() = "<F $cab $func $activate>"
    }

    private class AccessoryCommand(
        val address: Int,
        val subaddress: Int?,
        val activate: Int
    ) : Command() {
        constructor(linear_address: Int, activate: Int) : this(linear_address, null, activate)
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
        override fun toString() =
            if (subaddress == null) "<a $address $activate>"
            else "<a $address $subaddress $activate>"
    }

    private class WriteCvMainCommand(
        val cab: Int,
        val cv: Int,
        val bit: Int?,
        val value: Int
    ) : Command() {
        constructor(cab: Int, cv: Int, value: Int) : this(cab, cv, null, value)
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
        override fun toString() =
            if (bit == null) "<w $cab $cv $value>"
            else "<b $cab $cv $bit $value>"
    }

    private class WriteCvProgCommand(
        val cv: Int,
        val value: Int
    ) : Command() {
        override val resultRegex = """<r ($cv) (-?\d+)>"""
        override fun resultListener(groupValues: List<String>) {
            val cv = groupValues[1].toInt()
            val value = groupValues[2].toInt()
            if (BuildConfig.DEBUG) Log.i(TAG, String.format("Write CV %d result %d (prog)", cv, value))
            writeCvProgCallback?.invoke(cv, value)
        }
        override fun toString() = "<W $cv $value>"
    }

    private class ReadCvProgCommand(
        val cv: Int
    ) : Command() {
        private val callbacknum = 32767
        private val callbacksub = 0
        override val resultRegex = """<r($callbacknum)\|($callbacksub)\|($cv) (-?\d+)>"""
        override fun resultListener(groupValues: List<String>) {
            val cv = groupValues[3].toInt()
            val value = groupValues[4].toInt()
            if (BuildConfig.DEBUG) Log.i(TAG, String.format("Read CV %d result %d (prog)", cv, value))
            readCvProgCallback?.invoke(cv, value)
        }
        override fun toString() = "<R $cv $callbacknum $callbacksub>"
    }

    private class SpeedDCommand(
        val speed: String
    ) : Command() {
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
        override fun toString() = "<D $speed>"
    }

    //endregion

    open class CommandStationException: Exception()
}