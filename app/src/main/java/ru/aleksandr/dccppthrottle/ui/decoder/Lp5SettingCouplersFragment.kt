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
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Lp5SettingCouplersFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Lp5SettingsViewModel>()

    private val layoutId = R.layout.fragment_lp5_setting_couplers

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 246
        view.findViewById<PlusMinusView>(R.id.plusminusCv246)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(246, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(246)
            }
        }

        // 247
        view.findViewById<PlusMinusView>(R.id.plusminusCv247)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(247, it)
                    val seconds = Lp5SettingsViewModel.UNIT_16MSEC * it
                    view.findViewById<TextView>(R.id.textCv247)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(247)
            }
        }

        // 248
        view.findViewById<PlusMinusView>(R.id.plusminusCv248)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(248, it)
                    val seconds = Lp5SettingsViewModel.UNIT_16MSEC * it
                    view.findViewById<TextView>(R.id.textCv248)?.text =
                        getString(R.string.label_xp4_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(248)
            }
        }

        // 101
        view.findViewById<PlusMinusView>(R.id.plusminusCv101)?.apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(101, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(101)
            }
        }

    }
}