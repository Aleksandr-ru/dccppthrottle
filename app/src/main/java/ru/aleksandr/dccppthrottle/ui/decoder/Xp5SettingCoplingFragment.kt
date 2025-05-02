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
import ru.aleksandr.dccppthrottle.view.ByteSwitchView
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp5SettingCoplingFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SettingsViewModel>()

    private val layoutId = R.layout.fragment_xp5_setting_coupling

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // 130
        view.findViewById<PlusMinusView>(R.id.plusminusCv130)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(130, it)
                    val seconds = Xp5SettingsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv130)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(130).observe(viewLifecycleOwner) { value = it }
        }

        // 135
        view.findViewById<PlusMinusView>(R.id.plusminusCv135)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(135, it)
                }
            }
            model.liveCvValue(135).observe(viewLifecycleOwner) { value = it }
        }

        // 131
        view.findViewById<PlusMinusView>(R.id.plusminusCv131)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(131, it)
                }
            }
            model.liveCvValue(131).observe(viewLifecycleOwner) { value = it }
        }

        // 132
        view.findViewById<PlusMinusView>(R.id.plusminusCv132)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(132, it)
                    val seconds = Xp5SettingsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv132)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(132).observe(viewLifecycleOwner) { value = it }
        }

        // 133
        view.findViewById<PlusMinusView>(R.id.plusminusCv133)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(133, it)
                    val seconds = Xp5SettingsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv133)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(133).observe(viewLifecycleOwner) { value = it }
        }

        // 134
        view.findViewById<PlusMinusView>(R.id.plusminusCv134)?.apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(134, it)
                    val seconds = Xp5SettingsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv134)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(134).observe(viewLifecycleOwner) { value = it }
        }

    }
}