/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.cs

import android.os.Handler
import android.os.Looper
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.ConsoleStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.MainStore
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min
import kotlin.math.roundToInt

object CommandStation {
    private const val EMERGENCY_STOP = -1
    private const val MAX_SPEED_STEPS = 126
    private const val STATUS_TIMEOUT_MS = 5000L
    private const val CV_TIMEOUT_MS = 30000L
    private val statusResponse = Regex("<i(.+)>")
    private val handler = Handler(Looper.getMainLooper())
    private val nextReadId = AtomicLong(0)

    private var connection: BluetoothConnection? = null
    private var deviceName: String? = null
    private var statusRequests = createRequestQueue(stopOnTimeout = false)
    private var cvRequests = createRequestQueue(stopOnTimeout = true)

    fun isConnected() = connection != null
    fun getAddress() = connection?.getAddress()
    fun getName() = deviceName

    fun setConnection(conn: BluetoothConnection, name: String) = onMain {
        disconnect()
        statusRequests = createRequestQueue(stopOnTimeout = false)
        cvRequests = createRequestQueue(stopOnTimeout = true)
        connection = conn
        deviceName = name
        conn.setOnReceiveListener { message ->
            // Ignore callbacks from a replaced connection.
            if (connection === conn) {
                ConsoleStore.addIn(message)
                parseMessage(message)
            }
        }
    }

    fun disconnect() = onMain {
        val previous = connection
        connection = null
        deviceName = null
        previous?.setOnReceiveListener(null)
        previous?.close()
        statusRequests.close()
        cvRequests.close()
        ConsoleStore.clear()
    }

    fun getStatus(
        onError: ((Exception) -> Unit)? = null,
        callback: ((status: String) -> Unit)? = null
    ): RequestHandle = statusRequests.submit(
        "<s>", statusResponse, STATUS_TIMEOUT_MS,
        onError = { onError?.invoke(it) },
        onResult = { callback?.invoke(it[1]) }
    )

    fun getTrackCurrent() = sendCommand("<c>")

    fun setTrackPower(isOn: Boolean, join: Boolean = false) {
        val power = if (isOn) 1 else 0
        sendCommand(if (isOn && join) "<$power JOIN>" else "<$power>")
    }

    fun unassignAll() = sendCommand("<->")

    fun unassignLoco(slot: Int) {
        LocomotivesStore.getBySlot(slot)?.let { sendCommand("<- ${it.address}>") }
    }

    fun setLocomotiveSpeed(slot: Int, speed: Int, reverse: Boolean? = null) {
        LocomotivesStore.getBySlot(slot)?.let {
            val direction = if (reverse ?: it.reverse) 0 else 1
            sendCommand("<t ${it.slot} ${it.address} ${percentToSpeedSteps(speed)} $direction>")
        }
    }

    fun stopLocomotive(slot: Int) {
        LocomotivesStore.getBySlot(slot)?.let {
            val direction = if (it.reverse) 0 else 1
            sendCommand("<t ${it.slot} ${it.address} $EMERGENCY_STOP $direction>")
        }
    }

    fun emergencyStop() {
        sendCommand("<!>")
        LocomotivesStore.getSlots().forEach { LocomotivesStore.stopBySlot(it) }
    }

    fun setLocomotiveFunction(slot: Int, func: Int, isOn: Boolean) {
        LocomotivesStore.getBySlot(slot)?.let {
            sendCommand("<F ${it.address} $func ${if (isOn) 1 else 0}>")
        }
    }

    fun setAccessoryState(address: Int, isOn: Boolean) {
        sendCommand("<a $address ${if (isOn) 1 else 0}>")
        AccessoriesStore.setStateByAddress(address, isOn)
    }

    fun setCvMain(slot: Int, cv: Int, value: Int) {
        LocomotivesStore.getBySlot(slot)?.let { sendCommand("<w ${it.address} $cv $value>") }
    }

    fun setCvBitMain(slot: Int, cv: Int, bit: Int, value: Int) {
        LocomotivesStore.getBySlot(slot)?.let { sendCommand("<b ${it.address} $cv $bit $value>") }
    }

    fun setCvProg(
        cv: Int,
        value: Int,
        onError: ((Exception) -> Unit)? = null,
        callback: ((cv: Int, value: Int) -> Unit)? = null
    ): RequestHandle = cvRequests.submit(
        "<W $cv $value>", Regex("""<r ($cv) (-?\d+)>"""), CV_TIMEOUT_MS,
        onError = { error ->
            if (onError != null) onError(error) else callback?.invoke(cv, -1)
        },
        onResult = { callback?.invoke(cv, it[2].toIntOrNull() ?: -1) }
    )

    fun getCvProg(
        cv: Int,
        onError: ((Exception) -> Unit)? = null,
        callback: ((cv: Int, value: Int) -> Unit)? = null
    ): RequestHandle {
        // Keep both callback fields within 16-bit signed range, and do not reset
        // IDs on reconnect: a stale reply cannot match the next read of the same CV.
        val read = CvReadCommand(cv, nextReadId.getAndIncrement())
        return cvRequests.submit(
            read.command, read.response, CV_TIMEOUT_MS,
            onError = { error ->
                if (onError != null) onError(error) else callback?.invoke(cv, -1)
            },
            onResult = { callback?.invoke(cv, it[2].toIntOrNull() ?: -1) }
        )
    }

    fun setSpeedSteps(speedSteps: String) {
        if (speedSteps !in arrayOf("SPEED28", "SPEED128")) throw CommandStationException()
        sendCommand("<D $speedSteps>")
    }

    private fun createRequestQueue(stopOnTimeout: Boolean) = StationRequestQueue(
        send = ::sendWire,
        dispatch = ::onMain,
        schedule = { delay, action ->
            val task = Runnable { action() }
            handler.postDelayed(task, delay)
            val cancel: () -> Unit = { handler.removeCallbacks(task) }
            cancel
        },
        stopOnTimeout = stopOnTimeout
    )

    private fun onMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) action() else handler.post { action() }
    }

    private fun sendCommand(command: String) = onMain {
        if (connection != null) sendWire(command)
    }

    private fun sendWire(command: String) {
        val conn = connection ?: throw RequestDisconnectedException()
        conn.send(command)
        ConsoleStore.addOut(command)
    }

    private fun parseMessage(message: String) {
        when (val state = StationMessageParser.parse(message)) {
            is StationStateMessage.Power -> MainStore.setTrackPower(state.on, state.joined)
            is StationStateMessage.Throttle -> LocomotivesStore.setSpeedBySlot(
                state.slot, speedStepsToPercent(state.speed), state.reverse
            )
            is StationStateMessage.Functions -> LocomotivesStore.setAllFuncByAddress(
                state.address,
                BooleanArray(LocomotivesStore.FUNCTIONS_COUNT) { state.mask and (1 shl it) != 0 }
            )
            is StationStateMessage.Current -> MainStore.setTrackCurrent(
                mapOf("current" to state.current, "max" to state.max, "trip" to state.trip)
            )
            null -> {
                statusRequests.receive(message)
                cvRequests.receive(message)
            }
        }
    }

    private fun percentToSpeedSteps(percent: Int): Int =
        if (percent > 0) min(MAX_SPEED_STEPS.toFloat(), percent * MAX_SPEED_STEPS / 100F).roundToInt()
        else 0

    private fun speedStepsToPercent(speedSteps: Int): Int =
        if (speedSteps > 0) min(100F, speedSteps * 100F / MAX_SPEED_STEPS).roundToInt()
        else 0

    open class CommandStationException : Exception()
}
