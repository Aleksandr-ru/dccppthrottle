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
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteChipsView
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp4OutputFireboxFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_firebox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 181
        view.findViewById<ByteChipsView>(R.id.byteChipsCv181)?.apply {
            setOnChangeListener {
                model.setCvValue(181, it)
            }
            model.liveCvValue(181).observe(viewLifecycleOwner) { value = it }
        }

        val viewHi = view.findViewById<PlusMinusView>(R.id.plusminusCv182hi)
        val viewLo = view.findViewById<PlusMinusView>(R.id.plusminusCv182lo)
        val viewRes = view.findViewById<TextView>(R.id.textCv182)
        // 182-lo
        viewLo?.apply {
            setOnChangeListener {
                if (it !== null) {
                    if (it == 0) {
                        model.setCvValue(182, 0)
                        viewHi.isEnabled = false
                    } else {
                        if (BuildConfig.DEBUG) Log.d(TAG, String.format(
                            "CV182=%d, hi=%d, new lo=%d, result=%d",
                            model.getCvValue(182),
                            model.getCvValue(182).and(0b11110000),
                            it,
                            model.getCvValue(182).and(0b11110000) or it
                        ))
                        val value = model.getCvValue(182).and(0b11110000) or it
                        model.setCvValue(182, value)
                        if (!viewHi.isEnabled) {
                            viewHi.isEnabled = true
                            viewHi.change()
                        }

                    }
                    viewRes.text = getString(R.string.label_resulting_cv_x, model.getCvValue(182))
                }
            }
            model.liveCvValue(182).observe(viewLifecycleOwner) {
                if (BuildConfig.DEBUG) Log.d(TAG,
                    String.format("CV182=%d, lo=%d", it, it.and(0b00001111))
                )
                value = it.and(0b00001111)
            }
        }

        // 182-hi
        viewHi?.apply {
            setOnChangeListener {
                if (it !== null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, String.format(
                        "CV182=%d, new hi=%d, lo=%d, result=%d",
                        model.getCvValue(182),
                        it.shl(4),
                        model.getCvValue(182).and(0b00001111),
                        it.shl(4) or model.getCvValue(182).and(0b00001111)
                    ))
                    val value = it.shl(4) or model.getCvValue(182).and(0b00001111)
                    model.setCvValue(182, value)
                    viewRes.text = getString(R.string.label_resulting_cv_x, model.getCvValue(182))
                }
            }
            model.liveCvValue(182).observe(viewLifecycleOwner) {
                if (BuildConfig.DEBUG) Log.d(TAG,
                    String.format("CV182=%d, hi=%d", it, it.shr(4).and(0b0111))
                )
                value = it.shr(4).and(0b0111)
            }
        }
    }
}