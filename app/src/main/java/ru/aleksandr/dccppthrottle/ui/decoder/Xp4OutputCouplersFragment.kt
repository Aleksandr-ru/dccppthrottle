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

class Xp4OutputCouplersFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_couplers

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 124
        view.findViewById<PlusMinusView>(R.id.plusminusCv124)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(124, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(124)
            }
        }

        // 125
        view.findViewById<PlusMinusView>(R.id.plusminusCv125)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(125, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv125)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(125)
            }
        }

        // 126
        view.findViewById<PlusMinusView>(R.id.plusminusCv126)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(126, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv126)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(126)
            }
        }

        // 127
        view.findViewById<PlusMinusView>(R.id.plusminusCv127)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(127, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv127)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(127)
            }
        }

        // 128
        view.findViewById<PlusMinusView>(R.id.plusminusCv128)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(128, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(128)
            }
        }

        // 129
        view.findViewById<ByteChipsView>(R.id.byteChipsCv129)?.apply {
            setOnChangeListener {
                model.setCvValue(129, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(129)
            }
        }

        // 135
        view.findViewById<PlusMinusView>(R.id.plusminusCv135)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(135, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(135)
            }
        }

        // 136
        view.findViewById<PlusMinusView>(R.id.plusminusCv136)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(136, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv136)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(136)
            }
        }

        // 137
        view.findViewById<PlusMinusView>(R.id.plusminusCv137)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(137, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv137)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(137)
            }
        }
    }
}