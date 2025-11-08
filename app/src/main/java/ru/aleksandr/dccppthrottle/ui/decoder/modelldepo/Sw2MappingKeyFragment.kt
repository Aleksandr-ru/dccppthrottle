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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.ByteSwitchView

class Sw2MappingKeyFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Sw2MappingViewModel>()

    private val layoutId = R.layout.fragment_sw2_mapping_key

    private var keyIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)

        arguments?.getInt(ARG_INDEX)?.let {
            keyIndex = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cv = 165 + keyIndex * 4
        val keyNames = resources.getStringArray(R.array.sw2_mapping_keys)
        val outputNames = resources.getStringArray(R.array.sw2_outputs)

        view.findViewById<TextView>(R.id.textViewTitle)?.text = if (keyIndex > 1)
            getString(R.string.label_sw2_key_x_on, keyNames[keyIndex])
        else
            keyNames[keyIndex]


        view.findViewById<TextView>(R.id.textViewForwardCv1).text =
            getString(R.string.label_sw2_forward_cv_x, cv + 0)

        view.findViewById<ByteSwitchView>(R.id.byteForwardCv1)?.apply {
//            outputNames.slice(0 .. 7).withIndex().forEach {
//                setBitText(it.index, it.value)
//            }
            setOnChangeListener {
                model.setCvValue(cv + 0, it)
            }
            model.liveCvValue(cv + 0).observe(viewLifecycleOwner) { value = it }
        }

        view.findViewById<TextView>(R.id.textViewForwardCv2).text =
            getString(R.string.label_sw2_forward_cv_x, cv + 1)

        view.findViewById<ByteSwitchView>(R.id.byteForwardCv2)?.apply {
            outputNames.slice(8 .. 12).withIndex().forEach {
                setBitText(it.index, it.value)
            }
            setOnChangeListener {
                model.setCvValue(cv + 1, it)
            }
            model.liveCvValue(cv + 1).observe(viewLifecycleOwner) { value = it }
        }


        view.findViewById<TextView>(R.id.textViewReverseCv1).text =
            getString(R.string.label_sw2_reverse_cv_x, cv + 2)

        view.findViewById<ByteSwitchView>(R.id.byteReverseCv1)?.apply {
//            outputNames.slice(0 .. 7).withIndex().forEach {
//                setBitText(it.index, it.value)
//            }
            setOnChangeListener {
                model.setCvValue(cv + 2, it)
            }
            model.liveCvValue(cv + 2).observe(viewLifecycleOwner) { value = it }
        }

        view.findViewById<TextView>(R.id.textViewReverseCv2).text =
            getString(R.string.label_sw2_reverse_cv_x, cv + 3)

        view.findViewById<ByteSwitchView>(R.id.byteReverseCv2)?.apply {
            outputNames.slice(8 .. 12).withIndex().forEach {
                setBitText(it.index, it.value)
            }
            setOnChangeListener {
                model.setCvValue(cv + 3, it)
            }
            model.liveCvValue(cv + 3).observe(viewLifecycleOwner) { value = it }
        }

    }

    companion object {
        const val ARG_INDEX = "index"

        @JvmStatic
        fun newInstance(index: Int) = Sw2MappingKeyFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INDEX, index)
            }
        }
    }
}