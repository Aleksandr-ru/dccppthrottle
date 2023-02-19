package ru.aleksandr.dccppthrottle.cs

import androidx.lifecycle.MutableLiveData
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import kotlin.math.min

// https://github.com/DCC-EX/BaseStation-Classic/blob/master/DCCpp/SerialCommand.cpp#L82

object CommandStation {
    private const val EMERGENCY_STOP = -1
    private const val MAX_SPEED_STEPS = 126

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
    }

    fun isConnected() = connection != null

    private fun parseMessage(mes: String) {
        Regex("<p(0|1)>").matchEntire(mes)?.let {
            powerOn = it.groupValues[1].toInt() > 0
            return@parseMessage
        }
        Regex("""<T (\d+) (\d+) (0|1)>""").matchEntire(mes)?.let {
            val slot = it.groupValues[1].toInt()
            val speed = speedStepsToPercent(it.groupValues[2].toInt())
            val reverse = it.groupValues[3].toInt() == 0
            LocomotivesStore.setSpeedBySlot(slot, speed, reverse)
            return@parseMessage
        }
    }

    private fun percentToSpeedSteps(percent: Int) : Int {
        return if (percent > 0) {
            min(MAX_SPEED_STEPS, (MAX_SPEED_STEPS / 100) * percent)
        }
        else 0
    }

    private fun speedStepsToPercent(speedSteps: Int) : Int {
        return if (speedSteps > 0) {
            min(100, (speedSteps / MAX_SPEED_STEPS) * 100)
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

    // <t REGISTER CAB SPEED DIRECTION>
/*
 *    sets the throttle for a given register/cab combination
 *
 *    REGISTER: an internal register number, from 1 through MAX_MAIN_REGISTERS (inclusive), to store the DCC packet used to control this throttle setting
 *    CAB:  the short (1-127) or long (128-10293) address of the engine decoder
 *    SPEED: throttle speed from 0-126, or -1 for emergency stop (resets SPEED to 0)
 *    DIRECTION: 1=forward, 0=reverse.  Setting direction when speed=0 or speed=-1 only effects directionality of cab lighting for a stopped train
 *
 *    returns: <T REGISTER SPEED DIRECTION>
 *
 */

    fun setLocomotiveSpeed(slot: Int, speed: Int, reverse: Boolean? = null) {
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            val direction = (reverse ?: loco.reverse).let { rev ->
                if(rev) 0 else 1
            }
            //todo speed percent
            val command = ThrottleCommand(it.slot, it.address, percentToSpeedSteps(speed), direction)
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

    // <f CAB BYTE1 [BYTE2]>
/*
 *    turns on and off engine decoder functions F0-F28 (F0 is sometimes called FL)
 *    NOTE: setting requests transmitted directly to mobile engine decoder --- current state of engine functions is not stored by this program
 *
 *    CAB:  the short (1-127) or long (128-10293) address of the engine decoder
 *
 *    To set functions F0-F4 on (=1) or off (=0):
 *
 *    BYTE1:  128 + F1*1 + F2*2 + F3*4 + F4*8 + F0*16
 *    BYTE2:  omitted
 *
 *    To set functions F5-F8 on (=1) or off (=0):
 *
 *    BYTE1:  176 + F5*1 + F6*2 + F7*4 + F8*8
 *    BYTE2:  omitted
 *
 *    To set functions F9-F12 on (=1) or off (=0):
 *
 *    BYTE1:  160 + F9*1 +F10*2 + F11*4 + F12*8
 *    BYTE2:  omitted
 *
 *    To set functions F13-F20 on (=1) or off (=0):
 *
 *    BYTE1: 222
 *    BYTE2: F13*1 + F14*2 + F15*4 + F16*8 + F17*16 + F18*32 + F19*64 + F20*128
 *
 *    To set functions F21-F28 on (=1) of off (=0):
 *
 *    BYTE1: 223
 *    BYTE2: F21*1 + F22*2 + F23*4 + F24*8 + F25*16 + F26*32 + F27*64 + F28*128
 *
 *    returns: NONE
 *
 */

    fun setLocomotiveFunction(slot: Int, func: Int, isOn: Boolean) {
        LocomotivesStore.setFunctionBySlot(slot, func, isOn)
        val loco = LocomotivesStore.getBySlot(slot)
        loco?.let {
            var byte1 : Int = 0
            var byte2 : Int? = null
            when (func) {
                in (0..4) -> {
                    // BYTE1:  128 + F1*1 + F2*2 + F3*4 + F4*8 + F0*16
                    byte1 = 128
                    for(i in 1..4) if(it.functions[i]) byte1 += 1.shl(i-1)
                    if (it.functions[0]) byte1 += 1.shl(4)
                }
                in (5..8) -> {
                    // BYTE1:  176 + F5*1 + F6*2 + F7*4 + F8*8
                    byte1 = 176
                    for(i in 1..4) if(it.functions[i+4]) byte1 += 1.shl(i-1)
                }
                in (9..12) -> {
                    // BYTE1:  160 + F9*1 +F10*2 + F11*4 + F12*8
                    byte1 = 160
                    for(i in 1..4) if(it.functions[i+8]) byte1 += 1.shl(i-1)
                }
                in (13..20) -> {
                    // BYTE2: F13*1 + F14*2 + F15*4 + F16*8 + F17*16 + F18*32 + F19*64 + F20*128
                    byte1 = 222
                    byte2 = 0
                    for(i in 1..8) if(it.functions[i+12]) byte2 += 1.shl(i-1)
                }
                in (21..28) -> {
                    // BYTE2: F21*1 + F22*2 + F23*4 + F24*8 + F25*16 + F26*32 + F27*64 + F28*128
                    byte1 = 223
                    byte2 = 0
                    for(i in 1..8) if(it.functions[i+20]) byte2 += 1.shl(i-1)
                }
            }
            val command = FunctionCommand(it.address, byte1, byte2)
            sendCommand(command)
        }
    }

    // <a ADDRESS SUBADDRESS ACTIVATE>
/*
 *    turns an accessory (stationary) decoder on or off
 *
 *    ADDRESS:  the primary address of the decoder (0-511)
 *    SUBADDRESS: the subaddress of the decoder (0-3)
 *    ACTIVATE: 1=on (set), 0=off (clear)
 *
 *    Note that many decoders and controllers combine the ADDRESS and SUBADDRESS into a single number, N,
 *    from  1 through a max of 2044, where
 *
 *    N = (ADDRESS - 1) * 4 + SUBADDRESS + 1, for all ADDRESS>0
 *
 *    OR
 *
 *    ADDRESS = INT((N - 1) / 4) + 1
 *    SUBADDRESS = (N - 1) % 4
 *
 *    returns: NONE
 */
    fun setAccessoryState(address: Int, isOn: Boolean) {
        //todo subaddress?
        val activate = if(isOn) 1 else 0
        val command = AccessoryCommand(address, 0, activate)
        sendCommand(command)
        AccessoriesStore.setStateByAddress(address, isOn)
    }

    private interface Command {
        override fun toString(): String
    }

    private class PowerCommand(val p: Int) : Command {
        override fun toString() = "<$p>"
    }

    private class ThrottleCommand(
        val register: Int,
        val cab: Int,
        val speed: Int,
        val direction: Int
    ) : Command {
        override fun toString() = "<t $register $cab $speed $direction>"
    }

    private class FunctionCommand(
        val cab: Int,
        val byte1: Int,
        val byte2: Int?
    ) : Command {
        override fun toString() =
            if (byte2 == null) "<f $cab $byte1>"
            else "<f $cab $byte1 $byte2>"
    }

    private class AccessoryCommand(
        val address: Int,
        val subaddress: Int,
        val activate: Int
    ) : Command {
        override fun toString() = "<a $address $subaddress $activate>"
    }

    class CommandStationException: Exception()
}