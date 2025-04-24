/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteChipsView
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp4OutputServoFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_servo
    private val idx = Xp4OutputsViewModel.IDX_SERVO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false).apply {
        findViewById<TextView>(R.id.textViewTitle)?.text =
            resources.getStringArray(R.array.xp4_output_titles)[idx]
        findViewById<TextView>(R.id.textViewDescription)?.text =
            resources.getStringArray(R.array.xp4_output_description)[idx]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 166
        view.findViewById<ByteChipsView>(R.id.byteChipsCv166)?.apply {
            setOnChangeListener {
                model.setCvValue(166, it)
            }
            setOnChipCheckedListener { index, checked ->
                if (index == 0 && checked) value = value.and(1)
                else if (checked) value = value.and(1.inv())
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(166).and(0b00111110)
            }
        }

        // 167
        view.findViewById<PlusMinusView>(R.id.plusminusCv167)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(167, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(167)
            }
        }

        // 168
        view.findViewById<PlusMinusView>(R.id.plusminusCv168)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(168, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(168)
            }
        }

        // 160
        view.findViewById<PlusMinusView>(R.id.plusminusCv160)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(160, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(160)
            }
        }

        // 161
        view.findViewById<PlusMinusView>(R.id.plusminusCv161)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(161, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(161)
            }
        }

        // 162
        view.findViewById<PlusMinusView>(R.id.plusminusCv162)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(162, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv162)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(162)
            }
        }

        // 163
        view.findViewById<PlusMinusView>(R.id.plusminusCv163)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(163, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(163)
            }
        }

        // 164
        view.findViewById<PlusMinusView>(R.id.plusminusCv164)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(164, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(164)
            }
        }

        // 165
        view.findViewById<PlusMinusView>(R.id.plusminusCv165)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(165, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv165)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(165)
            }
        }
    }
}