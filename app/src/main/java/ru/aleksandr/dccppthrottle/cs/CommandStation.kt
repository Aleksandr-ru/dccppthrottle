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
        sendCommand(StatusCommand())
        sendCommand(UnassignAllCommand())
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

    fun setTrackPower(isOn: Boolean) {
        val power = if (isOn) 1 else 0
        val command = PowerCommand(power)
        sendCommand(command)
    }

    fun unassignLoco(address: Int) {
        val command = UnassignCommand(address)
        sendCommand(command)

        val slot = LocomotivesStore.getSlotByAddress(address)
        LocomotivesStore.stopBySlot(slot)
    }

    fun setLocomotiveSpeed(slot: Int, speed: Int, reverse: Boolean? = null) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val direction = (reverse ?: loco.reverse).let { rev ->
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
        LocomotivesStore.setFunctionBySlot(slot, func, isOn)
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
//            var byte1 : Int = 0
//            var byte2 : Int? = null
//            when (func) {
//                in (0..4) -> {
//                    // BYTE1:  128 + F1*1 + F2*2 + F3*4 + F4*8 + F0*16
//                    byte1 = 128
//                    for(i in 1..4) if(it.functions[i]) byte1 += 1.shl(i-1)
//                    if (it.functions[0]) byte1 += 1.shl(4)
//                }
//                in (5..8) -> {
//                    // BYTE1:  176 + F5*1 + F6*2 + F7*4 + F8*8
//                    byte1 = 176
//                    for(i in 1..4) if(it.functions[i+4]) byte1 += 1.shl(i-1)
//                }
//                in (9..12) -> {
//                    // BYTE1:  160 + F9*1 +F10*2 + F11*4 + F12*8
//                    byte1 = 160
//                    for(i in 1..4) if(it.functions[i+8]) byte1 += 1.shl(i-1)
//                }
//                in (13..20) -> {
//                    // BYTE2: F13*1 + F14*2 + F15*4 + F16*8 + F17*16 + F18*32 + F19*64 + F20*128
//                    byte1 = 222
//                    byte2 = 0
//                    for(i in 1..8) if(it.functions[i+12]) byte2 += 1.shl(i-1)
//                }
//                in (21..28) -> {
//                    // BYTE2: F21*1 + F22*2 + F23*4 + F24*8 + F25*16 + F26*32 + F27*64 + F28*128
//                    byte1 = 223
//                    byte2 = 0
//                    for(i in 1..8) if(it.functions[i+20]) byte2 += 1.shl(i-1)
//                }
//            }
//            val command = FunctionCommandLegacy(it.address, byte1, byte2)
            val active = if (isOn) 1 else 0
            val command = FunctionCommandEx(it.address, func, active)
            sendCommand(command)
        }
    }

    fun setAccessoryState(address: Int, isOn: Boolean) {
        //todo subaddress?
        val activate = if (isOn) 1 else 0
//        val command = AccessoryCommand(address, 0, activate)
        val command = AccessoryCommand(address, activate)
        sendCommand(command)
        AccessoriesStore.setStateByAddress(address, isOn)
    }

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

        Regex("""<l (\d+) (\d+) (\d+) (\d+)>""").matchEntire(mes)?.let {
            val addr = it.groupValues[1].toInt()
            val slot = it.groupValues[2].toInt() + 1
            val speedDir = it.groupValues[3].toInt()
            val speed = speedDir and 0b01111111
            val direction = speedDir.shr(7)
            val func = it.groupValues[4].toInt() // 536870911 max
            val funcStr = func.toString(2).padStart(LocomotivesStore.FUNCTIONS_COUNT, '0')
            val funcArr = funcStr.map { it == '1' }.reversed().toBooleanArray()
            val logArr = funcArr.mapIndexed{ index, b -> if (b) index else -1 }.filter { it > -1 }
            Log.i(TAG, "Cab $addr, slot: $slot, speed $speed, direction $direction, functions $logArr")
            LocomotivesStore.setSpeedBySlot(slot, speedStepsToPercent(speed), direction == 0)
            LocomotivesStore.setAllFuncBySlot(slot, funcArr)
            // todo TEST ME!
            return@parseMessage
        }

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
//        override val resultRegex = """<T (\d+) (-?\d+) (0|1)>"""
        override val resultRegex = """<T $register (-?\d+) (0|1)>"""
        override fun resultListener(groupValues: List<String>) {
//            val slot = groupValues[1].toInt()
//            val speedSteps = groupValues[2].toInt()
//            val speedPercent = speedStepsToPercent(speedSteps)
//            val reverse = groupValues[3].toInt() == 0
//            LocomotivesStore.setSpeedBySlot(slot, speedPercent, reverse)
            val speedSteps = groupValues[1].toInt()
            val speedPercent = speedStepsToPercent(speedSteps)
            val reverse = groupValues[2].toInt() == 0
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
        override val resultRegex: String? = null
        override fun resultListener(groupValues: List<String>) {}
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
        //override val resultRegex = """<r (\d{1,4}) (-?\d+)>"""
        override val resultRegex = """<r $cv (-?\d+)>"""
        override fun resultListener(groupValues: List<String>) {
//            val cv = groupValues[1].toInt()
//            val value = groupValues[2].toInt()
//            Log.i(TAG, String.format("Write CV %d result %d (prog)", cv, value))

            val value = groupValues[1].toInt()
            writeCvProgCallback?.invoke(cv, value)

            Log.i(TAG, String.format("Write CV %d result %d (prog)", cv, value))
        }
        override fun toString() = "<W $cv $value>"
    }

    private class ReadCvProgCommand(
        val cv: Int
    ) : Command() {
        override val resultRegex = """<r 32767 (-?\d+)>"""
        override fun resultListener(groupValues: List<String>) {
            val value = groupValues[2].toInt()
            readCvProgCallback?.invoke(cv, value)

            Log.i(TAG, String.format("Read CV result %d (prog)", value))
        }
        override fun toString() = "<R $cv 32767 0>"
    }

    /**
     * Exceptions
     */
    open class CommandStationException: Exception()
}