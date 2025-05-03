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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteChipsView
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp4OutputEsavingFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_esaving

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 183
        view.findViewById<ByteChipsView>(R.id.byteChipsCv183)?.apply {
            setOnChangeListener {
                model.setCvValue(183, it)
            }
            model.liveCvValue(183).observe(viewLifecycleOwner) { value = it }
        }

        // 184
        view.findViewById<PlusMinusView>(R.id.plusminusCv184)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(184, it)
                }
            }
            model.liveCvValue(184).observe(viewLifecycleOwner) { value = it }
        }

        // 185
        view.findViewById<PlusMinusView>(R.id.plusminusCv185)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(185, it)
                    val seconds = Xp4OutputsViewModel.UNIT_5MSEC * it
                    view.findViewById<TextView>(R.id.textCv185)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(185).observe(viewLifecycleOwner) { value = it }
        }
    }
}