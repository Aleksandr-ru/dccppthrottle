/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.modelldepo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteSwitchView

class Sw2SettingConfFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Sw2SettingsViewModel>()

    private val layoutId = R.layout.fragment_sw2_setting_conf

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
            model.liveCvValue(29).observe(viewLifecycleOwner) { value = it }
        }

    }
}