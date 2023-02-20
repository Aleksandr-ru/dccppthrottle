package ru.aleksandr.dccppthrottle.ui.cab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import kotlin.math.ceil


class LocoCabFragment : Fragment() {
    private val F_PER_ROW = 4

    private var slot: Int = 0

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
        val functionLayoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1F
        }
        val funcFormat = getString(R.string.label_f)
        val functionViews = Array<ToggleButton>(LocomotivesStore.FUNCTIONS_COUNT) { i ->
            ToggleButton(view.context).apply {
                text = String.format(funcFormat, i)
                textOn = text
                textOff = text
                tag = i
                layoutParams = functionLayoutParams
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
            val tableRow = TableRow(view.context)
            for (b in 0 until F_PER_ROW) {
                tableRow.addView(functionViews[i], b)
                i++
                if (i >= LocomotivesStore.FUNCTIONS_COUNT) break
            }
            tableLayout.addView(tableRow, r)
        }

        val speedView = view.findViewById<TextView>(R.id.textViewSpeed)
        val progressView = view.findViewById<SeekBar>(R.id.seekBar)
        val strStop = getString(R.string.speed_stop)
        progressView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var progress = 0

            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                if (value > 0) speedView.text = "$value%"
                else speedView.text = strStop
                progress = value
            }

            override fun onStartTrackingTouch(bar: SeekBar?) {
                // "Required but not yet implemented"
            }

            override fun onStopTrackingTouch(bar: SeekBar?) {
                bar?.let {
                    CommandStation.setLocomotiveSpeed(slot, bar.progress)
                }
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
            titleView.text = item.toString()
            addrView.text = item.address.toString()
            progressView.progress = item.speed
            revToggle.isChecked = item.reverse

            for ((i, b) in functionViews.withIndex()) {
                b.isChecked = item.functions[i]
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