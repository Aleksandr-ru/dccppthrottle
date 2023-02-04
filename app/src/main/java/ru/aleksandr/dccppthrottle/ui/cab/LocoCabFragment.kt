package ru.aleksandr.dccppthrottle.ui.cab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.aleksandr.dccppthrottle.R

private const val ARG_SLOT = "slot"

/**
 * A simple [Fragment] subclass.
 * Use the [LocoCabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocoCabFragment : Fragment() {
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
        val textView = view.findViewById<TextView>(R.id.textView)
        textView.text = slot.toString()
    }

    companion object {
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