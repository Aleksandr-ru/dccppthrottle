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
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp4OutputSmokeFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_smoke

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val outputs = resources.getStringArray(R.array.xp4_outputs)
        outputs[0] = getString(R.string.label_xp4_no_smoke)

        val viewSpinner = view.findViewById<Spinner>(R.id.spinnerOutput).apply {
            adapter =
                ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, outputs)

            model.liveCvValue(130).observe(viewLifecycleOwner) { setSelection(it % 8) }
        }
        val viewDelay = view.findViewById<PlusMinusView>(R.id.plusminusDelay).apply {
            model.liveCvValue(130).observe(viewLifecycleOwner) { value = it  / 16 }
        }
        val viewRes = view.findViewById<TextView>(R.id.textCv130)

        viewSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                viewDelay.isEnabled = position > 0
                if (position > 0 && viewDelay.value !== null) {
                    model.setCvValue(130, position + 16 * viewDelay.value!!)
                }
                else {
                    model.setCvValue(130, 0)
                }
                viewRes.text = getString(R.string.label_resulting_cv_x, model.getCvValue(130))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // do nothing
            }
        }

        viewDelay.setOnChangeListener {
            if (it !== null) {
                model.setCvValue(130, viewSpinner.selectedItemPosition + 16 * it)
                viewRes.text = getString(R.string.label_resulting_cv_x, model.getCvValue(130))

                val seconds = Xp4OutputsViewModel.UNIT_200MSEC * it
                view.findViewById<TextView>(R.id.textDelay)?.text =
                    getString(R.string.label_time_x_sec, seconds)
            }
        }

        view.findViewById<PlusMinusView>(R.id.plusminusCv131).apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(131, it)

                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv131)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(131).observe(viewLifecycleOwner) { value = it }
        }

        view.findViewById<PlusMinusView>(R.id.plusminusCv132).apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(132, it)
            }
            model.liveCvValue(132).observe(viewLifecycleOwner) { value = it }
        }

        view.findViewById<PlusMinusView>(R.id.plusminusCv133).apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(133, it)
            }
            model.liveCvValue(133).observe(viewLifecycleOwner) { value = it }
        }

        view.findViewById<PlusMinusView>(R.id.plusminusCv134).apply {
            setOnChangeListener {
                if (it !== null) {
                    model.setCvValue(134, it)

                    val seconds = Xp4OutputsViewModel.UNIT_100MSEC * it
                    view.findViewById<TextView>(R.id.textCv134)?.text =
                        getString(R.string.label_time_x_sec, seconds)
                }
            }
            model.liveCvValue(134).observe(viewLifecycleOwner) { value = it }
        }

    }
}