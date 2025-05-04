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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteSwitchView

class Xp4SettingConfFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4SettingsViewModel>()

    private val layoutId = R.layout.fragment_xp4_setting_conf

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

        // 50
        view.findViewById<ByteSwitchView>(R.id.byteCv50)?.apply {
            setOnChangeListener {
                model.setCvValue(50, it)
            }
            model.liveCvValue(50).observe(viewLifecycleOwner) { value = it }
        }

        // 51
        view.findViewById<ByteSwitchView>(R.id.byteCv51)?.apply {
            setOnChangeListener {
                model.setCvValue(51, it)
            }
            model.liveCvValue(51).observe(viewLifecycleOwner) { value = it }
        }

    }
}