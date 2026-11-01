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
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.Utility.remap
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.view.LockableScrollView
import kotlin.math.ceil
import kotlin.math.roundToInt

class CabFragment : Fragment() {
    private val TAG = javaClass.simpleName

    private var functionViews: List<ToggleButton> = listOf()
    private var funcNamesStr = ""

    private val F_PER_ROW by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)
        if (isSingle()) prefs.getString(getString(R.string.pref_key_f_per_row), null)?.toInt() ?: 4
        else 2
    }
    private val SPEED_KEYSTEP = 5
    private var speedSender: SpeedCommandThrottler? = null
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tableLayout = view.findViewById<TableLayout>(R.id.tableLayout)
        val progressView = view.findViewById<SeekBar>(R.id.seekBar)
        val revToggle = view.findViewById<ToggleButton>(R.id.toggleReverse)
        val titleView = view.findViewById<TextView>(R.id.textViewTitle)
        // optional views
        val addrView = view.findViewById<TextView>(R.id.textViewAddr)

        progressView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                val speed = speedForProgress(value)
                renderSpeedLabel(view, speed)
                if (fromUser) {
                    if (trackingSpeed) speedSender?.submit(speed)
                    else CommandStation.setLocomotiveSpeed(slot, speed)
                }
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {
                trackingSpeed = true
                cancelSpeedUpdates()
                speedSender = SpeedCommandThrottler(
                    send = { CommandStation.setLocomotiveSpeed(slot, it) }
                )
            }
            override fun onStopTrackingTouch(bar: SeekBar?) {
                trackingSpeed = false
                speedSender?.finish(speedForProgress(progressView.progress))
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

        var previousLoco: LocomotivesStore.LocomotiveState? = null
        var previousAddress: Int? = null
        var previousReverse: Boolean? = null
        LocomotivesStore.liveSlot(slot).observe(viewLifecycleOwner) { item ->
            progressView.isEnabled = item != null
            revToggle.isEnabled = item != null
            if (item == null) {
                cancelSpeedUpdates()
                previousLoco = null
                previousAddress = null
                previousReverse = null
                titleView.text = ""
                addrView?.text = ""
                progressView.progress = 0
                tableLayout.removeAllViews()
                functionViews = emptyList()
                funcNamesStr = ""
                return@observe
            }
            if (item !== previousLoco || item.address != previousAddress || item.reverse != previousReverse) {
                cancelSpeedUpdates()
            }
            previousLoco = item
            previousAddress = item.address
            previousReverse = item.reverse
            minSpeed = item.minSpeed
            maxSpeed = item.maxSpeed

            // Station replies must not move the slider away from the finger during a drag.
            if (!trackingSpeed || speedSender == null) {
                revToggle.isChecked = item.reverse
                progressView.progress = if (item.speed > 0) {
                    remap(item.speed.toFloat(), minSpeed.toFloat(), maxSpeed.toFloat(), 1F, 100F).roundToInt()
                } else 0
                renderSpeedLabel(view, item.speed)
            }
            titleView.text = item.toString()
            addrView?.text = item.address.toString()

            val newFuncStr = item.funcNames.mapIndexed { index, name ->
                name.takeIf { name.isNotEmpty() } ?: "F$index"
            }.joinToString()

            if (newFuncStr != funcNamesStr) {
                if (BuildConfig.DEBUG) Log.d(TAG,
                    "Recreate fKeys: $funcNamesStr -> $newFuncStr"
                )
                tableLayout.removeAllViews()
                functionViews = createFunctionViews(tableLayout)
                funcNamesStr = newFuncStr
            }
            for ((index, button) in functionViews.withIndex()) {
                button.isChecked = item.functions[index]
            }
        }
    }

    private fun cancelSpeedUpdates() {
        speedSender?.cancel()
        speedSender = null
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

    private fun speedForProgress(progress: Int): Int =
        if (progress <= 0) 0
        else remap(progress.toFloat(), 1F, 100F, minSpeed.toFloat(), maxSpeed.toFloat()).roundToInt()

    override fun onPause() {
        cancelSpeedUpdates()
        trackingSpeed = false
        super.onPause()
    }

    override fun onDestroyView() {
        cancelSpeedUpdates()
        trackingSpeed = false
        functionViews = emptyList()
        funcNamesStr = ""
        super.onDestroyView()
    }

    private fun createFunctionViews(tableLayout: TableLayout): List<ToggleButton> {
        // https://stackoverflow.com/a/2397869
        val tableLayoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        val loco = LocomotivesStore.getBySlot(slot)!!
        val functionViews = List(LocomotivesStore.FUNCTIONS_COUNT) { i ->
            ToggleButton(context).apply {
                text =
                    if (loco.funcNames[i].isEmpty()) getString(R.string.label_f, i)
                    else getString(R.string.label_f_named, i, loco.funcNames[i])
                textOn = text
                textOff = text

                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1F
                ).apply {
                    if (loco.funcNames[i].isNotEmpty()) span = F_PER_ROW
                }
                setOnClickListener {
                    val button = this
                    CommandStation.setLocomotiveFunction(slot, i, isChecked)
                    val selfResetTime = loco.funcReset[i]
                    if (isChecked && selfResetTime > 0) Handler(Looper.getMainLooper()).postDelayed({
                        CommandStation.setLocomotiveFunction(slot, i, false)
                        if (BuildConfig.DEBUG && !CommandStation.isConnected()) button.isChecked = false
                    }, selfResetTime.toLong())
                }
            }
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val namedOnly = prefs.getBoolean(getString(R.string.pref_key_only_named), false)

        val named = loco.funcNames.mapIndexedNotNull { index, s ->
            if (s.isEmpty()) null
            else index
        }
//        val unnamed = loco.funcNames.mapIndexedNotNull { index, s ->
//            if (s.isNotEmpty() || loco.namedOnly) null
//            else index
//        }
        val unnamed = loco.funcNames.mapIndexedNotNull { index, s ->
            if (s.isNotEmpty()) null
            else if (namedOnly && (named.size > 1 || named.size <= 1 && index > 12)) null
            else index
        }

        val rows = named.size + ceil(unnamed.size.toDouble() / F_PER_ROW.toDouble()).toInt()
        var i = 0
        for (r in 0 until rows) {
            val tableRow = TableRow(context).apply {
                layoutParams = tableLayoutParams
            }
            for (b in 0 until F_PER_ROW) {
                val f = if (i < named.size) named[i] else unnamed[i - named.size]
                tableRow.addView(functionViews[f], b)
                i++
                if (i >= named.size + unnamed.size) break
                else if (i <= named.size) break // big button
            }
            if (r >= named.size) {
                repeat(F_PER_ROW - tableRow.childCount) {
                    tableRow.addView(View(context).apply {
                        visibility = View.INVISIBLE
                        layoutParams = TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT, 0, 1F
                        )
                    })
                }
            }
            tableLayout.addView(tableRow, r)
        }

        return functionViews
    }

    // called from LocoCabActivity only
    fun onKeyDown(keyCode: Int): Boolean {
        val progressView = view?.findViewById<SeekBar>(R.id.seekBar)
        if (progressView != null && progressView.isEnabled && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            cancelSpeedUpdates()
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) progressView.progress -= SPEED_KEYSTEP
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) progressView.progress += SPEED_KEYSTEP

            val speed = speedForProgress(progressView.progress)
            //Log.i(TAG, "Speed to CS: $speed")
            CommandStation.setLocomotiveSpeed(slot, speed)
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
        fun newInstance(layout: Int, slot: Int) =
            CabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LAYOUT, layout)
                    putInt(ARG_SLOT, slot)
                }
            }
    }
}
