/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.cab

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.Utility.remap
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.view.LockableScrollView
import kotlin.math.ceil
import kotlin.math.roundToInt

class LocoCabFragment : Fragment() {
    private val TAG = javaClass.simpleName

    //private val F_PER_ROW = 4
    private val F_PER_ROW by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)
        if (isSingle()) prefs.getString(getString(R.string.pref_key_f_per_row), null)?.toInt() ?: 4
        else 2
    }
    private val SPEED_KEYSTEP = 5

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tableLayout = view.findViewById<TableLayout>(R.id.tableLayout)
        val functionViews = createFunctionViews(tableLayout)

        val progressView = view.findViewById<SeekBar>(R.id.seekBar)
        val revToggle = view.findViewById<ToggleButton>(R.id.toggleReverse)
        val titleView = view.findViewById<TextView>(R.id.textViewTitle)
        // optional views
        val speedView = view.findViewById<TextView>(R.id.textViewSpeed)
        val addrView = view.findViewById<TextView>(R.id.textViewAddr)

        progressView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var speed = 0

            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                if (value > 0) {
                    val scaledValue = remap(value.toFloat(), 1F, 100F, minSpeed.toFloat(), maxSpeed.toFloat())
                    speed = scaledValue.roundToInt()
                    speedView?.text = getString(R.string.speed_percent, speed)
                }
                else {
                    speed = 0
                    speedView?.text = getString(R.string.speed_stop)
                }

                if (!isSingle()) revToggle.text =
                    if (speed > 0)
                        if (revToggle.isChecked) getString(R.string.speed_rev, speed)
                        else getString(R.string.speed_fwd, speed)
                    else
                        if (revToggle.isChecked) getString(R.string.label_reverse)
                        else getString(R.string.label_forward)
            }

            override fun onStartTrackingTouch(bar: SeekBar?) { /* do nothing */ }

            override fun onStopTrackingTouch(bar: SeekBar?) {
                //Log.i(TAG, "Speed to CS: $speed")
                CommandStation.setLocomotiveSpeed(slot, speed)
            }
        })

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val scrollView = view as LockableScrollView
            progressView.setOnTouchListener { _, ev ->
                when (ev?.action) {
                    MotionEvent.ACTION_DOWN -> scrollView.setScrollingEnabled(false)
                    MotionEvent.ACTION_UP -> scrollView.setScrollingEnabled(true)
                }
                false
            }
            //TODO: same view for landscape and portrait
        }

        revToggle.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                CommandStation.setLocomotiveSpeed(slot, 0, isChecked)
            }
        }

        LocomotivesStore.liveSlot(slot).observe(viewLifecycleOwner) { item ->
            minSpeed = item.minSpeed
            maxSpeed = item.maxSpeed

            if (item.speed > 0) {
                val scaledSpeed = remap(item.speed.toFloat(), minSpeed.toFloat(), maxSpeed.toFloat(), 1F, 100F)
                progressView.progress = scaledSpeed.roundToInt()
            }
            else progressView.progress = 0

            revToggle.isChecked = item.reverse
            titleView.text = item.toString()
            addrView?.text = item.address.toString()

            for ((index, button) in functionViews.withIndex()) {
                button.isChecked = item.functions[index]
            }
        }
    }

    private fun createFunctionViews(tableLayout: TableLayout): List<ToggleButton> {
        // https://stackoverflow.com/a/2397869
        val tableLayoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        val tableRowLayoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1F
        }
        val loco = LocomotivesStore.getBySlot(slot)!!
        val functionViews = List(LocomotivesStore.FUNCTIONS_COUNT) { i ->
            ToggleButton(tableLayout.context).apply {
                text = if (loco.funcNames[i].isEmpty()) getString(R.string.label_f, i)
                else getString(R.string.label_f_named, i, loco.funcNames[i])
                textOn = text
                textOff = text
                tag = i
                layoutParams = tableRowLayoutParams.apply {
                    if (loco.funcNames[i].isNotEmpty()) span = F_PER_ROW
                }
                setOnCheckedChangeListener { button, isChecked ->
                    if (button.isPressed) {
                        CommandStation.setLocomotiveFunction(slot, i, isChecked)
                        val selfResetTime = loco.funcReset[i]
                        if (isChecked && selfResetTime > 0) Handler(Looper.getMainLooper()).postDelayed({
                            CommandStation.setLocomotiveFunction(slot, i, false)
                            if (BuildConfig.DEBUG && !CommandStation.isConnected()) button.isChecked = false
                        }, selfResetTime.toLong())
                    }
                }
            }
        }

        val named = loco.funcNames.mapIndexedNotNull { index, s ->
            if (s.isEmpty()) null
            else index
        }
        val unnamed = loco.funcNames.mapIndexedNotNull { index, s ->
            if (s.isNotEmpty() || loco.namedOnly) null
            else index
        }

        val rows = named.size + ceil(unnamed.size.toDouble() / F_PER_ROW.toDouble()).toInt()
        var i = 0
        for (r in 0 until rows) {
            val tableRow = TableRow(tableLayout.context).apply {
                layoutParams = tableLayoutParams
            }
            for (b in 0 until F_PER_ROW) {
                val f = if (i < named.size) named[i] else unnamed[i - named.size]
                tableRow.addView(functionViews[f], b)
                i++
                if (i >= LocomotivesStore.FUNCTIONS_COUNT) break
                else if (i <= named.size) break // big button
            }
            tableLayout.addView(tableRow, r)
        }

        return functionViews
    }

    // called from LocoCabActivity only
    fun onKeyDown(keyCode: Int): Boolean {
        val progressView = view?.findViewById<SeekBar>(R.id.seekBar)
        if (progressView != null && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) progressView.progress -= SPEED_KEYSTEP
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) progressView.progress += SPEED_KEYSTEP

            val scaledValue = remap(progressView.progress.toFloat(), 1F, 100F, minSpeed.toFloat(), maxSpeed.toFloat())
            val speed = scaledValue.roundToInt()
            //Log.i(TAG, "Speed to CS: $speed")
            CommandStation.setLocomotiveSpeed(slot, speed)
            return true
        }
        return false
    }

    companion object {

        const val ARG_LAYOUT = "layout"
        const val ARG_SLOT = "slot"

        @JvmStatic
        fun newInstance(layout: Int, slot: Int) =
            LocoCabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LAYOUT, layout)
                    putInt(ARG_SLOT, slot)
                }
            }
    }
}