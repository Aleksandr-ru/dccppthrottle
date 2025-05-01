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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteSwitchView

class Xp5SettingConfFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SettingsViewModel>()

    private val layoutId = R.layout.fragment_xp5_setting_conf

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 29
        view.findViewById<ByteSwitchView>(R.id.byteCv29)?.apply {
            setOnChangeListener {
                model.setCvValue(29, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(29)
            }
        }

        // 47
        view.findViewById<ByteSwitchView>(R.id.byteCv47)?.apply {
            setOnChangeListener {
                model.setCvValue(47, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(47)
            }
        }

        // 62
        view.findViewById<ByteSwitchView>(R.id.byteCv62)?.apply {
            setOnChangeListener {
                model.setCvValue(62, it)
            }
            model.loaded.observe(viewLifecycleOwner) {
                if (it) value = model.getCvValue(62)
            }
        }

    }
}