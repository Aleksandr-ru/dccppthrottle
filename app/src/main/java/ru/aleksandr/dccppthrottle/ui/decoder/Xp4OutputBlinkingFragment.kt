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

class Xp4OutputBlinkingFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_blinking
    private val idx = Xp4OutputsViewModel.IDX_BLINKING

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

        view.findViewById<ByteChipsView>(R.id.byteChipsCv109)?.apply {
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(109)
            }
            setOnChangeListener {
                model.setCvValue(109, it)
            }
        }
        view.findViewById<ByteChipsView>(R.id.byteChipsCv110)?.apply {
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(110)
            }
            setOnChangeListener {
                model.setCvValue(110, it)
            }
        }

        view.findViewById<PlusMinusView>(R.id.plusminusCv111)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(111, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv111)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(111)
            }
        }
        view.findViewById<PlusMinusView>(R.id.plusminusCv112)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(112, it)
                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv112)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(112)
            }
        }
    }
}