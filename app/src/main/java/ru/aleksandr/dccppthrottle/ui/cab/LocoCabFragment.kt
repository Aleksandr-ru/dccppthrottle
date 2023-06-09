/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.cab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.Utility.remap
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import kotlin.math.ceil
import kotlin.math.roundToInt

class LocoCabFragment : Fragment() {
    private val F_PER_ROW = 4

    private var slot: Int = 0
    private var minSpeed = 1
    private var maxSpeed = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            slot = it.getInt(ARG_SLOT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loco_cab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        val functionViews = Array<ToggleButton>(LocomotivesStore.FUNCTIONS_COUNT) { i ->
            ToggleButton(view.context).apply {
                text = getString(R.string.label_f, i)
                textOn = text
                textOff = text
                tag = i
                layoutParams = tableRowLayoutParams
                setOnCheckedChangeListener { button, isChecked ->
                    if (button.isPressed) {
                        CommandStation.setLocomotiveFunction(slot, i, isChecked)
                    }
                }
            }
        }

        val rows = ceil(LocomotivesStore.FUNCTIONS_COUNT.toDouble() / F_PER_ROW.toDouble()).toInt()
        val tableLayout = view.findViewById<TableLayout>(R.id.tableLayout)
        var i = 0
        for (r in 0 until rows) {
            val tableRow = TableRow(view.context).apply {
                layoutParams = tableLayoutParams
            }
            for (b in 0 until F_PER_ROW) {
                tableRow.addView(functionViews[i], b)
                i++
                if (i >= LocomotivesStore.FUNCTIONS_COUNT) break
                if (i == 1) break // big button
            }
            tableLayout.addView(tableRow, r)
        }

        val speedView = view.findViewById<TextView>(R.id.textViewSpeed)
        val progressView = view.findViewById<SeekBar>(R.id.seekBar)
        val strStop = getString(R.string.speed_stop)
        progressView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var speed = 0

            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                if (value > 0) {
                    val scaledValue = remap(value.toFloat(), 1F, 100F, minSpeed.toFloat(), maxSpeed.toFloat())
                    speed = scaledValue.roundToInt()
                    speedView.text = "$speed%"
                }
                else {
                    speed = 0
                    speedView.text = strStop
                }
            }

            override fun onStartTrackingTouch(bar: SeekBar?) {
                // "Required but not yet implemented"
            }

            override fun onStopTrackingTouch(bar: SeekBar?) {
                CommandStation.setLocomotiveSpeed(slot, speed)
            }
        })

        val revToggle = view.findViewById<ToggleButton>(R.id.toggleReverse)
        revToggle.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                CommandStation.setLocomotiveSpeed(slot, 0, isChecked)
            }
        }

        val addrView = view.findViewById<TextView>(R.id.textViewAddr)
        val titleView = view.findViewById<TextView>(R.id.textViewTitle)

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
            addrView.text = item.address.toString()

            for ((index, button) in functionViews.withIndex()) {
                button.isChecked = item.functions[index]
            }
        }
    }

    companion object {

        const val ARG_SLOT = "slot"

        @JvmStatic
        fun newInstance(slot: Int) =
            LocoCabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SLOT, slot)
                }
            }
    }
}