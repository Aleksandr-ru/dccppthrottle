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

class Xp4OutputFadingFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_fading

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 186
        view.findViewById<ByteChipsView>(R.id.byteChipsOutputs)?.apply {
            setOnChangeListener {
                model.setCvValue(186, it)
            }
            model.liveCvValue(186).observe(viewLifecycleOwner) { value = it }
        }

        // 187
        view.findViewById<PlusMinusView>(R.id.plusminusDelay)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(187, it)
                    val seconds = Xp4OutputsViewModel.UNIT_10MSEC * it
                    view.findViewById<TextView>(R.id.textDelay)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(187).observe(viewLifecycleOwner) { value = it }
        }

    }
}