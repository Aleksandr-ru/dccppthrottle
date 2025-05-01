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

class Xp5SettingLampsFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SettingsViewModel>()

    private val layoutId = R.layout.fragment_xp5_setting_lamps

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 172
        view.findViewById<PlusMinusView>(R.id.plusminusCv172)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(172, it)
                    val seconds = Xp5SettingsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv172)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(172)
            }
        }

        // 170
        view.findViewById<PlusMinusView>(R.id.plusminusCv170)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(170, it)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(170)
            }
        }

        // 171
        view.findViewById<PlusMinusView>(R.id.plusminusCv171)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(171, it)
                    val seconds = Xp5SettingsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv171)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(171)
            }
        }


    }
}