/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

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

class Xp4OutputNeonFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_neon

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 188
        view.findViewById<ByteChipsView>(R.id.byteChipsCv188)?.apply {
            setOnChangeListener {
                model.setCvValue(188, it)
            }
            model.liveCvValue(188).observe(viewLifecycleOwner) { value = it }
        }

        // 189
        view.findViewById<PlusMinusView>(R.id.plusminusCv189)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(189, it)
                    val seconds = Xp4OutputsViewModel.UNIT_5MSEC * it
                    view.findViewById<TextView>(R.id.textCv189)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(189).observe(viewLifecycleOwner) { value = it }
        }

        // 190
        view.findViewById<PlusMinusView>(R.id.plusminusCv190)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(190, it)
                }
            }
            model.liveCvValue(190).observe(viewLifecycleOwner) { value = it }
        }
    }
}