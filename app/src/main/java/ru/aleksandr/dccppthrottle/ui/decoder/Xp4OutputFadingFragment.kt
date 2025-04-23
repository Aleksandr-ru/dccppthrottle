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

class Xp4OutputFadingFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_fading
    private val idx = Xp4OutputsViewModel.IDX_FADING

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

        view.findViewById<ByteChipsView>(R.id.byteChipsOutputs)?.apply {
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(186)
            }
            setOnChangeListener {
                model.setCvValue(186, it)
            }
        }

        view.findViewById<PlusMinusView>(R.id.plusminusDelay)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(187, it)
                    val seconds = Xp4OutputsViewModel.UNIT_10MSEC * it
                    view.findViewById<TextView>(R.id.textDelay)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(187)
            }
        }
    }
}