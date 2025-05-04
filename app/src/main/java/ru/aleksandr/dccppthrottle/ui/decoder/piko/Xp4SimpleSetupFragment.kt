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
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteSwitchView

class Xp4SimpleSetupFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4SimpleMappingViewModel>()

    private val layoutId = R.layout.fragment_xp4_simple_setup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 96
        view.findViewById<SwitchCompat>(R.id.switchCv96)?.apply {
            setOnCheckedChangeListener { button, checked ->
                val value = if (checked) 0 else 1
                model.setCvValue(96, value)
            }
            model.liveCvValue(96).observe(viewLifecycleOwner) { isChecked = it == 0 }
        }

        val outputNames = resources.getStringArray(R.array.xp4_outputs).apply {
            val disabledStr = getString(R.string.placeholder_output_deactivated)
            set(0, disabledStr)
        }
        val keyNames = resources.getStringArray(R.array.xp4_simple_keys).run {
            val disabledStr = getString(R.string.placeholder_f_key_deactivated)
            set(1, disabledStr)
            slice(1 until size)
        }

        // 107
        val cv107outputSpinner = view.findViewById<Spinner>(R.id.spinnerCv107output).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, outputNames)
        }
        val cv107keySpinner = view.findViewById<Spinner>(R.id.spinnerCv107key).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, keyNames)
        }
        cv107outputSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (position == 0) {
                    cv107keySpinner.setSelection(0)
                }
                else if (position > 0 && cv107keySpinner.selectedItemPosition == 0) {
                    cv107keySpinner.setSelection(1)
                }
                val value = position * 16 + cv107keySpinner.selectedItemPosition
                if (BuildConfig.DEBUG) Log.d(TAG, "CV107 = $value")
                model.setCvValue(107, value)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { /* do notning */ }
        }
        cv107keySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (position == 0) {
                    cv107outputSpinner.setSelection(0)
                }
                else if (position > 0 && cv107outputSpinner.selectedItemPosition == 0) {
                    cv107outputSpinner.setSelection(1)
                }
                val value = cv107outputSpinner.selectedItemPosition * 16 + position
                if (BuildConfig.DEBUG) Log.d(TAG, "CV107 = $value")
                model.setCvValue(107, value)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { /* do notning */ }
        }
        model.liveCvValue(107).observe(viewLifecycleOwner) {
            val outputIndex = it / 16
            val keyIndex = it % 16
            if (outputIndex < outputNames.size && keyIndex < keyNames.size) {
                cv107outputSpinner.setSelection(outputIndex)
                cv107keySpinner.setSelection(keyIndex)
            }
        }

        // 108
        val cv108outputSpinner = view.findViewById<Spinner>(R.id.spinnerCv108output).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, outputNames)
        }
        val cv108keySpinner = view.findViewById<Spinner>(R.id.spinnerCv108key).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, keyNames)
        }
        cv108outputSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (position == 0) {
                    cv108keySpinner.setSelection(0)
                }
                else if (position > 0 && cv108keySpinner.selectedItemPosition == 0) {
                    cv108keySpinner.setSelection(1)
                }
                val value = position * 16 + cv108keySpinner.selectedItemPosition
                if (BuildConfig.DEBUG) Log.d(TAG, "onItemSelected CV108=$value")
                model.setCvValue(108, value)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { /* do noting */ }
        }
        cv108keySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                if (position == 0) {
                    cv108outputSpinner.setSelection(0)
                }
                else if (position > 0 && cv108outputSpinner.selectedItemPosition == 0) {
                    cv108outputSpinner.setSelection(1)
                }
                val value = cv108outputSpinner.selectedItemPosition * 16 + position
                if (BuildConfig.DEBUG) Log.d(TAG, "onItemSelected CV108=$value")
                model.setCvValue(108, value)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { /* do noting */ }
        }
        model.liveCvValue(108).observe(viewLifecycleOwner) {
            val outputIndex = it / 16
            val keyIndex = it % 16
            if (outputIndex < outputNames.size && keyIndex < keyNames.size) {
                cv108outputSpinner.setSelection(outputIndex)
                cv108keySpinner.setSelection(keyIndex)
            }
        }

        // 113
        view.findViewById<ByteSwitchView>(R.id.byteCv113)?.apply {
            setOnChangeListener {
                model.setCvValue(113, it)
            }
            model.liveCvValue(113).observe(viewLifecycleOwner) { value = it }
        }

        // 114
        view.findViewById<ByteSwitchView>(R.id.byteCv114)?.apply {
            setOnChangeListener {
                model.setCvValue(114, it)
            }
            model.liveCvValue(114).observe(viewLifecycleOwner) { value = it }
        }

    }
}