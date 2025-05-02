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

class Xp5SettingFlashingFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SettingsViewModel>()

    private val layoutId = R.layout.fragment_xp5_setting_flashing

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 173
        view.findViewById<PlusMinusView>(R.id.plusminusCv173)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(173, it)
                    val seconds = Xp5SettingsViewModel.UNIT_20MSEC * it
                    view.findViewById<TextView>(R.id.textCv173)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(173).observe(viewLifecycleOwner) { value = it }
        }

        // 174
        view.findViewById<PlusMinusView>(R.id.plusminusCv174)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(174, it)
                    val seconds = Xp5SettingsViewModel.UNIT_20MSEC * it
                    view.findViewById<TextView>(R.id.textCv174)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(174).observe(viewLifecycleOwner) { value = it }
        }

        // 175
        view.findViewById<PlusMinusView>(R.id.plusminusCv175)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(175, it)
                    val seconds = Xp5SettingsViewModel.UNIT_20MSEC * it
                    view.findViewById<TextView>(R.id.textCv175)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(175).observe(viewLifecycleOwner) { value = it }
        }

        // 176
        view.findViewById<PlusMinusView>(R.id.plusminusCv176)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(176, it)
                    val seconds = Xp5SettingsViewModel.UNIT_20MSEC * it
                    view.findViewById<TextView>(R.id.textCv176)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(176).observe(viewLifecycleOwner) { value = it }
        }

    }
}