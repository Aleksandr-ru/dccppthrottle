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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp5SettingServoFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SettingsViewModel>()

    private val layoutId = R.layout.fragment_xp5_setting_servo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // servo 1
        // 202
        view.findViewById<PlusMinusView>(R.id.plusminusCv202)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(202, it)
            }
            model.liveCvValue(202).observe(viewLifecycleOwner) { value = it }
        }

        // 203
        view.findViewById<PlusMinusView>(R.id.plusminusCv203)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(203, it)
            }
            model.liveCvValue(203).observe(viewLifecycleOwner) { value = it }
        }

        // 204
        view.findViewById<PlusMinusView>(R.id.plusminusCv204)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(204, it)
            }
            model.liveCvValue(204).observe(viewLifecycleOwner) { value = it }
        }

        // servo 2
        // 208
        view.findViewById<PlusMinusView>(R.id.plusminusCv208)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(208, it)
            }
            model.liveCvValue(208).observe(viewLifecycleOwner) { value = it }
        }

        // 209
        view.findViewById<PlusMinusView>(R.id.plusminusCv209)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(209, it)
            }
            model.liveCvValue(209).observe(viewLifecycleOwner) { value = it }
        }

        // 210
        view.findViewById<PlusMinusView>(R.id.plusminusCv210)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(210, it)
            }
            model.liveCvValue(210).observe(viewLifecycleOwner) { value = it }
        }

        // servo 3
        // 214
        view.findViewById<PlusMinusView>(R.id.plusminusCv214)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(214, it)
            }
            model.liveCvValue(214).observe(viewLifecycleOwner) { value = it }
        }

        // 215
        view.findViewById<PlusMinusView>(R.id.plusminusCv215)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(215, it)
            }
            model.liveCvValue(215).observe(viewLifecycleOwner) { value = it }
        }

        // 216
        view.findViewById<PlusMinusView>(R.id.plusminusCv216)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(216, it)
            }
            model.liveCvValue(216).observe(viewLifecycleOwner) { value = it }
        }

        // servo 4
        // 220
        view.findViewById<PlusMinusView>(R.id.plusminusCv220)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(220, it)
            }
            model.liveCvValue(220).observe(viewLifecycleOwner) { value = it }
        }

        // 221
        view.findViewById<PlusMinusView>(R.id.plusminusCv221)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(221, it)
            }
            model.liveCvValue(221).observe(viewLifecycleOwner) { value = it }
        }

        // 222
        view.findViewById<PlusMinusView>(R.id.plusminusCv222)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(222, it)
            }
            model.liveCvValue(222).observe(viewLifecycleOwner) { value = it }
        }

    }
}