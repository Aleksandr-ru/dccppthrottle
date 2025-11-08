/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.modelldepo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteSwitchView
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Sw2SettingsModeFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Sw2SettingsViewModel>()

    private val layoutId = R.layout.fragment_sw2_settings_mode

    private var modeIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)

        arguments?.getInt(ARG_INDEX)?.let {
            modeIndex = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cvs = Sw2SettingsViewModel.modeCvs[modeIndex]
        val modeNames = resources.getStringArray(R.array.sw2_conf_tabs).drop(1)
        val keyNames = resources.getStringArray(R.array.sw2_mode_keys)
        val outputNames = resources.getStringArray(R.array.sw2_outputs)

        view.findViewById<TextView>(R.id.textViewTitle)?.text = modeNames[modeIndex]

        view.findViewById<TextView>(R.id.textCvModeKey).text =
            getString(R.string.label_sw2_cv_mode_key_x, cvs[Sw2SettingsViewModel.CV_INDEX_MODEKEY])

        view.findViewById<Spinner>(R.id.spinnerCvModeKey).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                keyNames
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    model.setCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_MODEKEY], position)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // do nothing
                }
            }

            model.liveCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_MODEKEY]).observe(viewLifecycleOwner) {
                if (it < keyNames.size) {
                    setSelection(it)
                }
            }
        }


        view.findViewById<TextView>(R.id.textModeChCv1).text =
            getString(R.string.label_sw2_cv_x, cvs[Sw2SettingsViewModel.CV_INDEX_CHANNELS1])

        view.findViewById<TextView>(R.id.textModeChCv2).text =
            getString(R.string.label_sw2_cv_x, cvs[Sw2SettingsViewModel.CV_INDEX_CHANNELS2])

        view.findViewById<ByteSwitchView>(R.id.byteModeChCv1)?.apply {
//            outputNames.slice(0 .. 7).withIndex().forEach {
//                setBitText(it.index, it.value)
//            }
            setOnChangeListener {
                model.setCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_CHANNELS1], it)
            }
            model.liveCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_CHANNELS1]).observe(viewLifecycleOwner) { value = it }
        }

        view.findViewById<ByteSwitchView>(R.id.byteModeChCv2)?.apply {
            outputNames.slice(8 .. 12).withIndex().forEach {
                setBitText(it.index, it.value)
            }
            setOnChangeListener {
                model.setCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_CHANNELS2], it)
            }
            model.liveCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_CHANNELS2]).observe(viewLifecycleOwner) { value = it }
        }


        view.findViewById<TextView>(R.id.textBrightness).text =
            getString(R.string.label_sw2_mode_brightness_x, cvs[Sw2SettingsViewModel.CV_INDEX_BRIGHTNESS])

        view.findViewById<ByteSwitchView>(R.id.byteModeBrightnessCv)?.apply {
            setOnChangeListener {
                model.setCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_BRIGHTNESS], it)
            }
            model.liveCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_BRIGHTNESS]).observe(viewLifecycleOwner) { value = it }
        }


        view.findViewById<TextView>(R.id.textNumCh).text =
            getString(R.string.label_sw2_mode_num_ch_x, cvs[Sw2SettingsViewModel.CV_INDEX_NUM_CH])

        view.findViewById<PlusMinusView>(R.id.plusminusNumChCv).apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_NUM_CH], it)
                }
            }
            model.liveCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_NUM_CH]).observe(viewLifecycleOwner) { value = it }
        }


        view.findViewById<TextView>(R.id.textModeTimer).text =
            getString(R.string.label_sw2_mode_timer_x, cvs[Sw2SettingsViewModel.CV_INDEX_TIMER])

        view.findViewById<PlusMinusView>(R.id.plusminusModeTimer).apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_TIMER], it)
                    val seconds = Sw2SettingsViewModel.UNIT_420MSEC * it
                    view.findViewById<TextView>(R.id.textModeTimerDelay)?.text =
                        getString(R.string.label_time_from_to_sec_xx, seconds, seconds * 2)
                }
            }
            model.liveCvValue(cvs[Sw2SettingsViewModel.CV_INDEX_TIMER]).observe(viewLifecycleOwner) { value = it }
        }


    }

    companion object {
        const val ARG_INDEX = "index"

        @JvmStatic
        fun newInstance(index: Int) = Sw2SettingsModeFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INDEX, index)
            }
        }
    }
}