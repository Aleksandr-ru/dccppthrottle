/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteSwitchView

class Xp4SimpleKeyFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4SimpleMappingViewModel>()

    private val layoutId = R.layout.fragment_xp4_simple_key

    private var keyIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)

        arguments?.getInt(ARG_INDEX)?.let {
            keyIndex = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cv = 33 + keyIndex
        val keyNames = resources.getStringArray(R.array.xp4_simple_keys)
        val outputNames = resources.getStringArray(R.array.xp4_simple_outputs)

        view.findViewById<TextView>(R.id.textViewTitle)?.text =
            getString(R.string.label_xp4_key_x, keyNames[keyIndex])
        view.findViewById<TextView>(R.id.textViewDesc)?.text =
            getString(R.string.label_xp4_simple_key_cv, cv)

        val byteOutputs = view.findViewById<ByteSwitchView>(R.id.byteView)!!.apply {
            setOnChangeListener {
                model.setCvValue(cv, it)
            }
            model.liveCvValue(cv).observe(viewLifecycleOwner) { value = it }
        }

        val shiftMap = Xp4SimpleMappingViewModel.shiftMap[keyIndex]

        val toggleShift = view.findViewById<ToggleButton>(R.id.toggleShift)!!.apply {
            setOnCheckedChangeListener { button, checked ->
                if (button.isPressed) {
                    val value = if (checked) {
                        model.getCvValue(shiftMap.first).or(1 shl shiftMap.second)
                    } else {
                        model.getCvValue(shiftMap.first).and((1 shl shiftMap.second).inv())
                    }
                    if (BuildConfig.DEBUG) Log.d(
                        TAG,
                        "Shift for CV $cv: CV ${shiftMap.first}.${shiftMap.second}=$checked, value=$value"
                    )
                    model.setCvValue(shiftMap.first, value)
                }
            }
        }

        model.liveCvValue(shiftMap.first).observe(viewLifecycleOwner) {
            toggleShift.isChecked = it and (1 shl shiftMap.second) > 0
            val shift = if (toggleShift.isChecked) 3 else 0
            for (i in 0 .. 7) {
                byteOutputs.setBitText(i, outputNames[i + shift])
            }
        }

    }

    companion object {
        const val ARG_INDEX = "index"

        @JvmStatic
        fun newInstance(index: Int) = Xp4SimpleKeyFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INDEX, index)
            }
        }
    }
}