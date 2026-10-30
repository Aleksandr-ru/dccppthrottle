/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.cab

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.preference.PreferenceManager
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.view.LockableScrollView

class CabFragment : Fragment() {
    private var functionViews: Map<Int, ToggleButton> = emptyMap()
    private var functionLayout: FunctionLayout? = null
    private var currentState: CabUiState? = null
    private var functionsPerRow = 4
    private var namedFunctionsOnly = false
    private val speedKeyStep = 5
    private var speedSender: SpeedCommandThrottler? = null
    private var speedTargetValid: (() -> Boolean)? = null
    private var trackingSpeed = false

    private var slot: Int = 0
    private var minSpeed = 1
    private var maxSpeed = 100
    private var layoutId: Int = 0
    private fun isSingle() = layoutId == R.layout.fragment_loco_cab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            layoutId = it.getInt(ARG_LAYOUT)
            slot = it.getInt(ARG_SLOT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(layoutId, container, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        readFunctionPreferences()
        val tableLayout = view.findViewById<TableLayout>(R.id.tableLayout)
        val progressView = view.findViewById<SeekBar>(R.id.seekBar)
        val revToggle = view.findViewById<ToggleButton>(R.id.toggleReverse)
        val titleView = view.findViewById<TextView>(R.id.textViewTitle)
        val addrView = view.findViewById<TextView>(R.id.textViewAddr)

        progressView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                val speed = SpeedScale.toSpeed(value, minSpeed, maxSpeed)
                renderSpeedLabel(view, speed)
                if (fromUser) {
                    // Accessibility/keyboard changes may have no touch start/stop callbacks.
                    if (!trackingSpeed && speedTargetValid?.invoke() != true) beginSpeedUpdates()
                    speedSender?.submit(speed)
                }
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {
                trackingSpeed = true
                beginSpeedUpdates()
            }
            override fun onStopTrackingTouch(bar: SeekBar?) {
                trackingSpeed = false
                speedSender?.flush(SpeedScale.toSpeed(progressView.progress, minSpeed, maxSpeed))
                cancelSpeedUpdates()
            }
        })

        progressView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> (view as? LockableScrollView)?.setScrollingEnabled(false)
                MotionEvent.ACTION_UP -> (view as? LockableScrollView)?.setScrollingEnabled(true)
                MotionEvent.ACTION_CANCEL -> {
                    cancelSpeedUpdates()
                    (view as? LockableScrollView)?.setScrollingEnabled(true)
                }
            }
            false
        }

        revToggle.setOnClickListener {
            cancelPendingSpeedUpdates(parentFragmentManager, slot)
            CommandStation.setLocomotiveSpeed(slot, 0, revToggle.isChecked)
        }

        LocomotivesStore.data.map { CabUiState.forSlot(it, slot) }.distinctUntilChanged()
            .observe(viewLifecycleOwner) { state ->
                val previous = currentState
                currentState = state
                progressView.isEnabled = state != null
                revToggle.isEnabled = state != null
                if (state == null) {
                    cancelSpeedUpdates()
                    titleView.text = ""
                    addrView?.text = ""
                    progressView.progress = 0
                    tableLayout.removeAllViews()
                    functionViews = emptyMap()
                    functionLayout = null
                    return@observe
                }

                val replaced = previous == null || state.id != previous.id
                if (replaced || speedTargetValid?.invoke() == false) cancelSpeedUpdates()
                if (replaced || state.title != previous?.title) titleView.text = state.title
                if (replaced || state.address != previous?.address) addrView?.text = state.address.toString()
                if (replaced || state.motion != previous?.motion) {
                    minSpeed = state.motion.minSpeed
                    maxSpeed = state.motion.maxSpeed
                    // Replies describe already sent intermediate speeds, not the finger's
                    // current position. Keep the local preview while a valid drag is active.
                    if (!trackingSpeed || speedTargetValid?.invoke() != true) {
                        if (revToggle.isChecked != state.motion.reverse) revToggle.isChecked = state.motion.reverse
                        val progress = SpeedScale.toProgress(state.motion.speed, minSpeed, maxSpeed)
                        if (progressView.progress != progress) progressView.progress = progress
                        renderSpeedLabel(view, state.motion.speed)
                    }
                }
                if (replaced || state.functionNames != previous?.functionNames) {
                    updateFunctionLayout(tableLayout, state)
                }
                state.changedFunctions(previous).forEach { index ->
                    functionViews[index]?.let { button ->
                        if (button.isChecked != state.functions[index]) button.isChecked = state.functions[index]
                    }
                }
            }
    }

    private fun beginSpeedUpdates() {
        cancelSpeedUpdates()
        val loco = LocomotivesStore.getBySlot(slot) ?: return
        val locoId = loco.id
        val reverse = loco.reverse
        val address = CommandStation.getAddress()
        val valid = {
            val current = LocomotivesStore.getBySlot(slot)
            current?.id == locoId && current.reverse == reverse &&
                CommandStation.isConnected() && CommandStation.getAddress() == address
        }
        speedTargetValid = valid
        val handler = Handler(Looper.getMainLooper())
        speedSender = SpeedCommandThrottler(
            now = SystemClock::uptimeMillis,
            schedule = { delay, action ->
                val task = Runnable { action() }
                handler.postDelayed(task, delay)
                val cancel: () -> Unit = { handler.removeCallbacks(task) }
                cancel
            },
            send = { CommandStation.setLocomotiveSpeed(slot, it) },
            isValid = valid
        )
    }

    private fun cancelSpeedUpdates() {
        speedSender?.cancel()
        speedSender = null
        speedTargetValid = null
    }

    private fun renderSpeedLabel(view: View, speed: Int) {
        view.findViewById<TextView>(R.id.textViewSpeed)?.let {
            val text = if (speed > 0) getString(R.string.speed_percent, speed) else getString(R.string.speed_stop)
            if (it.text != text) it.text = text
        }
        if (!isSingle()) {
            view.findViewById<ToggleButton>(R.id.toggleReverse).let {
                val text = if (speed > 0) {
                    getString(if (it.isChecked) R.string.speed_rev else R.string.speed_fwd, speed)
                } else getString(if (it.isChecked) R.string.label_reverse else R.string.label_forward)
                if (it.text != text) it.text = text
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Returning from Settings can change layout without changing a locomotive.
        readFunctionPreferences()
        currentState?.let { state ->
            view?.findViewById<TableLayout>(R.id.tableLayout)?.let { updateFunctionLayout(it, state) }
        }
    }

    private fun readFunctionPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        functionsPerRow = if (isSingle()) {
            (prefs.getString(getString(R.string.pref_key_f_per_row), null)?.toIntOrNull() ?: 4).coerceAtLeast(1)
        } else 2
        namedFunctionsOnly = prefs.getBoolean(getString(R.string.pref_key_only_named), false)
    }

    private fun updateFunctionLayout(table: TableLayout, state: CabUiState) {
        val layout = FunctionLayout(state.functionNames, functionsPerRow, namedFunctionsOnly)
        if (layout == functionLayout) return
        table.removeAllViews()
        val buttons = mutableMapOf<Int, ToggleButton>()
        layout.rows().forEach { indexes ->
            val row = TableRow(requireContext()).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
                )
            }
            indexes.forEach { index ->
                val button = ToggleButton(requireContext()).apply {
                    val name = state.functionNames[index]
                    val label = if (name.isEmpty()) getString(R.string.label_f, index)
                        else getString(R.string.label_f_named, index, name)
                    textOn = label
                    textOff = label
                    text = label
                    isChecked = state.functions[index]
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT
                    ).apply {
                        weight = 1F
                        if (name.isNotEmpty()) span = functionsPerRow
                    }
                    setOnClickListener {
                        // Read the current timer, not the object captured when buttons were built.
                        val loco = LocomotivesStore.getBySlot(slot) ?: return@setOnClickListener
                        CommandStation.setLocomotiveFunction(slot, index, isChecked)
                        val resetTime = loco.funcReset[index]
                        val locoId = loco.id
                        if (isChecked && resetTime > 0) Handler(Looper.getMainLooper()).postDelayed({
                            if (LocomotivesStore.getBySlot(slot)?.id == locoId) {
                                CommandStation.setLocomotiveFunction(slot, index, false)
                                if (BuildConfig.DEBUG && !CommandStation.isConnected()) {
                                    functionViews[index]?.isChecked = false
                                }
                            }
                        }, resetTime.toLong())
                    }
                }
                row.addView(button)
                buttons[index] = button
            }
            table.addView(row)
        }
        functionViews = buttons
        functionLayout = layout
    }

    override fun onPause() {
        cancelSpeedUpdates()
        trackingSpeed = false
        super.onPause()
    }

    override fun onDestroyView() {
        cancelSpeedUpdates()
        trackingSpeed = false
        functionViews = emptyMap()
        functionLayout = null
        currentState = null
        super.onDestroyView()
    }

    fun onKeyDown(keyCode: Int): Boolean {
        val progressView = view?.findViewById<SeekBar>(R.id.seekBar)
        if (progressView != null && progressView.isEnabled &&
            (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            cancelSpeedUpdates()
            progressView.progress += if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) speedKeyStep else -speedKeyStep
            CommandStation.setLocomotiveSpeed(slot, SpeedScale.toSpeed(progressView.progress, minSpeed, maxSpeed))
            return true
        }
        return false
    }

    companion object {
        const val ARG_LAYOUT = "layout"
        const val ARG_SLOT = "slot"

        internal fun cancelPendingSpeedUpdates(manager: FragmentManager, slot: Int? = null) {
            manager.fragments.filterIsInstance<CabFragment>().forEach { cab ->
                if (slot == null || cab.slot == slot) cab.cancelSpeedUpdates()
            }
        }

        @JvmStatic
        fun newInstance(layout: Int, slot: Int) = CabFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_LAYOUT, layout)
                putInt(ARG_SLOT, slot)
            }
        }
    }
}
