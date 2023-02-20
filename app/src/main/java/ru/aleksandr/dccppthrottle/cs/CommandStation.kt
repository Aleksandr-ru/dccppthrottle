package ru.aleksandr.dccppthrottle.cs

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import kotlin.math.min

// https://github.com/DCC-EX/BaseStation-Classic/blob/master/DCCpp/SerialCommand.cpp#L82

object CommandStation {
    private const val EMERGENCY_STOP = -1
    private const val MAX_SPEED_STEPS = 126

    private val TAG = javaClass.simpleName

    private var connection: BluetoothConnection? = null
    private val read: MutableLiveData<String> = MutableLiveData()
    private val write: MutableLiveData<String> = MutableLiveData()

    private var powerOn = false

    fun setConnection(conn: BluetoothConnection) {
        connection = conn
        connection!!.setOnReceiveListener {
            read.postValue(it)
            parseMessage(it)
        }
        connection?.send(UnassignAllCommand().toString())
    }

    fun isConnected() = connection != null

    private fun parseMessage(mes: String) {
        Regex("<p(0|1)>").matchEntire(mes)?.let {
            powerOn = it.groupValues[1].toInt() > 0
            return@parseMessage
        }
        Regex("""<T (\d+) (-?\d+) (0|1)>""").matchEntire(mes)?.let {
            val slot = it.groupValues[1].toInt()
            val speedSteps = it.groupValues[2].toInt()
            val speedPercent = speedStepsToPercent(speedSteps)
            val reverse = it.groupValues[3].toInt() == 0
            LocomotivesStore.setSpeedBySlot(slot, speedPercent, reverse)
            return@parseMessage
        }
        Regex("""<l (\d+) (\d+) (\d+) (\d+)>""").matchEntire(mes)?.let {
            val addr = it.groupValues[1].toInt()
            val slot = it.groupValues[2].toInt() + 1
            val speedDir = it.groupValues[3].toInt()
            val speed = speedDir and 0b01111111
            val direction = speedDir.shr(7)
            val func = it.groupValues[4].toInt()
            Log.i(TAG, "Cab $addr, slot: $slot, speed $speed, direction $direction, functions $func")
            // todo parse func bit use it
            // https://dcc-ex.com/reference/software/command-reference.html
            return@parseMessage
        }
        Regex("""<c CurrentMAIN (\d+) C Milli (\d) (\d+) (0|1) (\d+)>""").matchEntire(mes)?.let {
            val currentMa = it.groupValues[1].toInt()
            val maxMa = it.groupValues[3].toInt()
            val tripMa = it.groupValues[5].toInt()
            Log.i(TAG, String.format("Current: %d mA, max: %d mA, trip: %d mA", currentMa, maxMa, tripMa))
            // todo show current
            // https://dcc-ex.com/reference/software/command-reference.html
            return@parseMessage
        }
        Regex("""<r 32767 (-?\d+)>""").matchEntire(mes)?.let {
            val value = it.groupValues[2].toInt()
            Log.i(TAG, String.format("Read CV result %d (prog)", value))
            // todo use it
            // https://dcc-ex.com/reference/software/command-reference.html
            return@parseMessage
        }
        Regex("""<r (\d+) (-?\d+)>""").matchEntire(mes)?.let {
            val cv = it.groupValues[1].toInt()
            val value = it.groupValues[2].toInt()
            Log.i(TAG, String.format("Write CV %d result %d (prog)", cv, value))
            // todo use it
            // https://dcc-ex.com/reference/software/command-reference.html
            return@parseMessage
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

    private fun sendCommand(command: Command) {
        val str = command.toString()
        connection?.let {
            it.send(str)
            write.postValue(str)
        }
    }

    fun disconnect() {
        connection?.apply {
            setOnReceiveListener(null)
            close()
        }
        connection = null
    }

    fun setTrackPower(isOn: Boolean) {
        val power = if(isOn) 1 else 0
        val command = PowerCommand(power)
        sendCommand(command)
    }

    fun unassignLoco(address: Int) {
        val command = UnassignCommand(address)
        sendCommand(command)
    }

    fun setLocomotiveSpeed(slot: Int, speed: Int, reverse: Boolean? = null) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val direction = (reverse ?: loco.reverse).let { rev ->
                if(rev) 0 else 1
            }
            val speedSteps = percentToSpeedSteps(speed)
            val command = ThrottleCommand(it.slot, it.address, speedSteps, direction)
            sendCommand(command)
        }
    }

    fun stopLocomotive(slot: Int) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val direction = if(it.reverse) 0 else 1
            val command = ThrottleCommand(it.slot, it.address, EMERGENCY_STOP, direction)
            sendCommand(command)
        }
    }

    fun emergencyStop() {
        val command = EmergencyCommand()
        sendCommand(command)
        // todo speed 0
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
        val activate = if(isOn) 1 else 0
//        val command = AccessoryCommand(address, 0, activate)
        val command = AccessoryCommand(address, activate)
        sendCommand(command)
        AccessoriesStore.setStateByAddress(address, isOn)
    }

    private interface Command {
        override fun toString(): String
    }

    private data class PowerCommand(val p: Int) : Command {
        override fun toString() = "<$p>"
    }

    private class CurrentCommand() : Command {
        override fun toString() = "<c>"
    }

    private data class UnassignCommand(val address: Int) : Command {
        override fun toString() = "<- $address>"
    }

    private class UnassignAllCommand() : Command {
        override fun toString() = "<->"
    }

    private class EmergencyCommand() : Command {
        override fun toString() = "<!>"
    }

    private data class ThrottleCommand(
        val register: Int,
        val cab: Int,
        val speed: Int,
        val direction: Int
    ) : Command {
        override fun toString() = "<t $register $cab $speed $direction>"
    }

    private data class FunctionCommandLegacy(
        val cab: Int,
        val byte1: Int,
        val byte2: Int?
    ) : Command {
        override fun toString() =
            if (byte2 == null) "<f $cab $byte1>"
            else "<f $cab $byte1 $byte2>"
    }

    private data class FunctionCommandEx(
        val cab: Int,
        val func: Int,
        val activate: Int
    ) : Command {
        override fun toString() = "<F $cab $func $activate>"
    }

    private data class AccessoryCommand(
        val address: Int,
        val subaddress: Int?,
        val activate: Int
    ) : Command {
        constructor(linear_address: Int, activate: Int) : this(linear_address, null, activate)
        override fun toString() =
            if (subaddress == null) "<a $address $activate>"
            else "<a $address $subaddress $activate>"
    }

    private data class WriteCvMain(
        val cab: Int,
        val cv: Int,
        val bit: Int?,
        val value: Int
    ) : Command {
        constructor(cab: Int, cv: Int, value: Int) : this(cab, cv, null, value)
        override fun toString() =
            if (bit == null) "<w $cab $cv $value>"
            else "<w $cab $cv $bit $value>"
    }

    private data class WriteCvProg(
        val cv: Int,
        val value: Int
    ) : Command {
        override fun toString() = "<W $cv $value>"
    }

    private data class ReadCvProg(
        val cv: Int
    ) : Command {
        override fun toString() = "<R $cv 32767 0>"
    }

    open class CommandStationException: Exception()
}