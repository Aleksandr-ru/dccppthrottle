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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp5SettingSwoffFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp5SettingsViewModel>()

    private val layoutId = R.layout.fragment_xp5_setting_swoff

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<LinearLayout>(R.id.layout)
        resources.getStringArray(R.array.xp5_outputs).forEachIndexed { index, name ->
            val cv = 180 + index
            val listItem = layoutInflater.inflate(R.layout.fragment_xp5_setting_swoff_item, null)
            listItem.findViewById<TextView>(R.id.textViewTitle)?.apply {
                text = getString(R.string.label_xp5_swoff_item, cv, name)
            }
            listItem.findViewById<PlusMinusView>(R.id.plusminusDelay)?.apply {
                setOnChangeListener {
                    if (it !== null) {
                        model.setCvValue(cv, it)
                        val seconds = Xp5SettingsViewModel.UNIT_05SEC * it
                        listItem.findViewById<TextView>(R.id.textDelay)?.text =
                            getString(R.string.label_time_x_sec, seconds)
                    }
                }
                model.liveCvValue(cv).observe(viewLifecycleOwner) { value = it }
            }
            listView.addView(listItem)
        }

    }
}