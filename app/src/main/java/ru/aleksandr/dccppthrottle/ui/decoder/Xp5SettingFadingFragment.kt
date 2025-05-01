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

class Xp5SettingFadingFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SettingsViewModel>()

    private val layoutId = R.layout.fragment_xp5_setting_fading

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<PlusMinusView>(R.id.plusminusCv177)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(177, it)
                    val seconds = Xp5SettingsViewModel.UNIT_20MSEC * it
                    view.findViewById<TextView>(R.id.textCv177)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(177)
            }
        }

        view.findViewById<PlusMinusView>(R.id.plusminusCv178)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(178, it)
                    val seconds = Xp5SettingsViewModel.UNIT_20MSEC * it
                    view.findViewById<TextView>(R.id.textCv178)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(178)
            }
        }

    }
}