package ru.aleksandr.dccppthrottle.cs

import android.util.Log
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.ConsoleStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.MainStore
import kotlin.math.min

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

    /**
     * Public methods
     */

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

    fun setTrackPower(isOn: Boolean) {
        val power = if (isOn) 1 else 0
        val command = PowerCommand(power)
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
            val command = FunctionCommandEx(it.address, func, active)
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

    /**
     * Private methods
     */

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
            Regex(com.resultRegex!!).matchEntire(mes)?.let {
                com.resultListener(it.groupValues)
                com.processed = true // to avoid ConcurrentModificationException
                return@forEach
            }
        }
    }

    private fun percentToSpeedSteps(percent: Int) : Int {
        return if (percent > 0) {
            val speedSteps = percent * MAX_SPEED_STEPS / 100
            min(MAX_SPEED_STEPS, speedSteps)
        }
        else 0
    }

    private fun speedStepsToPercent(speedSteps: Int) : Int {
        return if (speedSteps > 0) {
            val percent = speedSteps * 100 / MAX_SPEED_STEPS
            min(100, percent)
        }
        else 0
    }

    /**
     * Commands
     */

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
            Log.i(TAG, groupValues[1])
            statusCallback?.invoke(groupValues[1])
        }
        override fun toString() = "<s>"
    }

    private class PowerCommand(val p: Int) : Command() {
        override val resultRegex = "<p(0|1)>"
        override fun resultListener(groupValues: List<String>) {
            val power = groupValues[1].toInt() > 0
            MainStore.setTrackPower(power)
        }
        override fun toString() = "<$p>"
    }

    private class CurrentCommand() : Command() {
        override val resultRegex = """<c CurrentMAIN (\d+) C Milli (\d) (\d+) (0|1) (\d+)>"""
        override fun resultListener(groupValues: List<String>) {
            val currentMa = groupValues[1].toInt()
            val maxMa = groupValues[3].toInt()
            val tripMa = groupValues[5].toInt()
            MainStore.setTrackCurrent(mapOf("current" to currentMa, "max" to maxMa, "trip" to tripMa))
            Log.i(TAG, String.format("Current: %d mA, max: %d mA, trip: %d mA", currentMa, maxMa, tripMa))
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
            Log.i(TAG, "Slot: $slot, speed $speedSteps, reverse $reverse")
            LocomotivesStore.setSpeedBySlot(register, speedPercent, reverse)
        }
        override fun toString() = "<t $register $cab $speed $direction>"
    }

    private class FunctionCommandLegacy(
        val cab: Int,
        val byte1: Int,
        val byte2: Int?
    ) : Command() {
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
        override fun toString() =
            if (byte2 == null) "<f $cab $byte1>"
            else "<f $cab $byte1 $byte2>"
    }

    private class FunctionCommandEx(
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
            val logArr = funcArr.mapIndexed{ index, b -> if (b) index else -1 }.filter { it > -1 }
            Log.i(TAG, "Cab $addr, slot: $slot, speed $speed, direction $direction, functions $logArr")
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
            else "<w $cab $cv $bit $value>"
    }

    private class WriteCvProgCommand(
        val cv: Int,
        val value: Int
    ) : Command() {
        override val resultRegex = """<r ($cv) (-?\d+)>"""
        override fun resultListener(groupValues: List<String>) {
            val cv = groupValues[1].toInt()
            val value = groupValues[2].toInt()
            Log.i(TAG, String.format("Write CV %d result %d (prog)", cv, value))
            writeCvProgCallback?.invoke(cv, value)
        }
        override fun toString() = "<W $cv $value>"
    }

    private class ReadCvProgCommand(
        val cv: Int
    ) : Command() {
        override val resultRegex = """<r 32767 (-?\d+)>"""
        override fun resultListener(groupValues: List<String>) {
            val value = groupValues[2].toInt()
            Log.i(TAG, String.format("Read CV result %d (prog)", value))
            readCvProgCallback?.invoke(cv, value)
        }
        override fun toString() = "<R $cv 32767 0>"
    }

    private class SpeedDCommand(
        val speed: String
    ) : Command() {
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
        override fun toString() = "<D $speed>"
    }

    /**
     * Exceptions
     */
    open class CommandStationException: Exception()
}