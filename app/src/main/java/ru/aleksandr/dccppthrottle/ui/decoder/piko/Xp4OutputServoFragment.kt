/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteChipsView
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp4OutputServoFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_servo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 166
        view.findViewById<ByteChipsView>(R.id.byteChipsCv166)?.apply {
            setChipText(0, getString(R.string.label_xp4_output_susi))
            setOnChipCheckedListener { index, checked ->
                if (index == 0 && checked) value = value.and(1)
                else if (checked) value = value.and(1.inv())
            }
            setOnChangeListener {
                model.setCvValue(166, it)
            }
            model.liveCvValue(166).observe(viewLifecycleOwner) {
                value = it.and( 0b11000001)
                if (BuildConfig.DEBUG) Log.d(TAG, "liveCvValue(166) = $it, byteChipsCv166 = $value")
            }
        }

        // 167
        view.findViewById<PlusMinusView>(R.id.plusminusCv167)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(167, it)
            }
            model.liveCvValue(167).observe(viewLifecycleOwner) { value = it }
        }

        // 168
        view.findViewById<PlusMinusView>(R.id.plusminusCv168)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(168, it)
            }
            model.liveCvValue(168).observe(viewLifecycleOwner) { value = it }
        }

        // 160
        view.findViewById<PlusMinusView>(R.id.plusminusCv160)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(160, it)
            }
            model.liveCvValue(160).observe(viewLifecycleOwner) { value = it }
        }

        // 161
        view.findViewById<PlusMinusView>(R.id.plusminusCv161)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(161, it)
            }
            model.liveCvValue(161).observe(viewLifecycleOwner) { value = it }
        }

        // 162
        view.findViewById<PlusMinusView>(R.id.plusminusCv162)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(162, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv162)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(162).observe(viewLifecycleOwner) { value = it }
        }

        // 163
        view.findViewById<PlusMinusView>(R.id.plusminusCv163)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(163, it)
            }
            model.liveCvValue(163).observe(viewLifecycleOwner) { value = it }
        }

        // 164
        view.findViewById<PlusMinusView>(R.id.plusminusCv164)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(164, it)
            }
            model.liveCvValue(164).observe(viewLifecycleOwner) { value = it }
        }

        // 165
        view.findViewById<PlusMinusView>(R.id.plusminusCv165)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(165, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv165)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(165).observe(viewLifecycleOwner) { value = it }
        }

    }
}