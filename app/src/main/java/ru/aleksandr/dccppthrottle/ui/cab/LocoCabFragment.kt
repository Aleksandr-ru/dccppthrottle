package ru.aleksandr.dccppthrottle.ui.cab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import kotlin.math.ceil

/**
 * A simple [Fragment] subclass.
 * Use the [LocoCabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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
        val functionViews = Array<ToggleButton>(LocomotivesStore.FUNCTIONS_COUNT) { i ->
            ToggleButton(view.context).apply {
                text = "F$i"
                textOn = "F$i"
                textOff = "F$i"
                tag = i
                setOnCheckedChangeListener { button, isChecked ->
                    if (button.isPressed) {
                        Toast.makeText(button.context, "Function ${button.tag} ($i) is $isChecked", Toast.LENGTH_SHORT).show()
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

        LocomotivesStore.liveSlot(slot).observe(viewLifecycleOwner) { item ->
            val titleView = view.findViewById<TextView>(R.id.textViewTitle)
            titleView.text = item.toString()
            val addrView = view.findViewById<TextView>(R.id.textViewAddr)
            addrView.text = item.address.toString()

            val speedView = view.findViewById<TextView>(R.id.textViewSpeed)
            val revToggle = view.findViewById<ToggleButton>(R.id.toggleReverse)
            speedView.text = item.speed.toString() + "%"
            revToggle.isChecked = item.reverse

            for ((i, b) in functionViews.withIndex()) {
                b.isChecked = item.functions[i]
            }
        }
    }

    companion object {

        const val ARG_SLOT = "slot"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param slot Parameter 1.
         * @return A new instance of fragment LocoCabFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(slot: Int) =
            LocoCabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SLOT, slot)
                }
            }
    }
}