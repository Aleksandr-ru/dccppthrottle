/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.BuildConfig
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
            model.liveCvValue(29).observe(viewLifecycleOwner) { value = it }
        }

        // 47
        view.findViewById<ByteSwitchView>(R.id.byteCv47)?.apply {
            setOnChangeListener {
                model.setCvValue(47, it)
            }
            model.liveCvValue(47).observe(viewLifecycleOwner) {
                if (BuildConfig.DEBUG) Log.d(TAG, "CV 47 live value = $it")
                value = it
            }
        }

        // 62
        view.findViewById<ByteSwitchView>(R.id.byteCv62)?.apply {
            setOnChangeListener {
                model.setCvValue(62, it)
            }
            model.liveCvValue(62).observe(viewLifecycleOwner) { value = it }
        }

    }
}