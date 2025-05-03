/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteSwitchView

class Xp5SimpleSetupFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SimpleMappingViewModel>()

    private val layoutId = R.layout.fragment_xp5_simple_setup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 96
        view.findViewById<SwitchCompat>(R.id.switchCv96)?.apply {
            setOnCheckedChangeListener { button, checked ->
                val value = if (checked) 1 else 6
                model.setCvValue(96, value)
            }
            model.liveCvValue(96).observe(viewLifecycleOwner) { isChecked = it == 1 }
        }

        val outputNames = resources.getStringArray(R.array.xp5_outputs).slice(2 .. 8)

        // 97
        view.findViewById<ByteSwitchView>(R.id.byteCv97)?.apply {
            hiddenBits = 0b10000000
            for (i in 0 .. 6) {
                setBitText(i, outputNames[i])
            }
            setOnChangeListener {
                model.setCvValue(97, it)
            }
            model.liveCvValue(97).observe(viewLifecycleOwner) { value = it }
        }

        // 98
        view.findViewById<ByteSwitchView>(R.id.byteCv98)?.apply {
            hiddenBits = 0b10000000
            for (i in 0 .. 6) {
                setBitText(i, outputNames[i])
            }
            setOnChangeListener {
                model.setCvValue(98, it)
            }
            model.liveCvValue(98).observe(viewLifecycleOwner) { value = it }
        }

    }
}