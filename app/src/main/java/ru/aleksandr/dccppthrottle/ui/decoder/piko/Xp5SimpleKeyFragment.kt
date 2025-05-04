/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

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

class Xp5SimpleKeyFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SimpleMappingViewModel>()

    private val layoutId = R.layout.fragment_xp5_simple_key

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

        val cv = 33 + keyIndex
        val keyNames = resources.getStringArray(R.array.xp5_simple_keys)
        val outputNames = resources.getStringArray(R.array.xp5_outputs).run {
            val start = (keyIndex / 5) * 3
            slice(start until size)
        }

        val title = getString(R.string.label_xp5_key_x_cv_y, keyNames[keyIndex], cv)
        view.findViewById<TextView>(R.id.textViewTitle)?.text = title

        view.findViewById<ByteSwitchView>(R.id.byteView)?.apply {
            for (i in 0 .. 7) {
                if (i < outputNames.size) setBitText(i, outputNames[i])
                else hiddenBits += 1.shl(i)
            }
            setOnChangeListener {
                model.setCvValue(cv, it)
            }
            model.liveCvValue(cv).observe(viewLifecycleOwner) { value = it }
        }

    }

    companion object {
        const val ARG_INDEX = "index"

        @JvmStatic
        fun newInstance(index: Int) = Xp5SimpleKeyFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INDEX, index)
            }
        }
    }
}