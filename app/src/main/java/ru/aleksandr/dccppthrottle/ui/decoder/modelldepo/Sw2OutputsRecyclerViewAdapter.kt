/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.modelldepo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentSw2OutputsItemBinding
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Sw2OutputsRecyclerViewAdapter(
    private val model: Sw2OutputsViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<Sw2OutputsRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(binding: FragmentSw2OutputsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val outputView: TextView = binding.textOutput
        val effectText: TextView = binding.textEffect
        val effectView: Spinner = binding.spinnerEffect
        val dayRightnessText: TextView = binding.textDayBrightness
        val dayBrightnessView: PlusMinusView = binding.plusminusDayBrightness
        val nightBrightnessText: TextView = binding.textNightBrightness
        val nightBrightnessView: PlusMinusView = binding.plusminusNightBrightness
        val fadeSpeedText: TextView = binding.textFadeSpeed
        val fadeSpeedView: PlusMinusView = binding.plusminusFadeSpeed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentSw2OutputsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = 13

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val outputNames = context.resources.getStringArray(R.array.sw2_outputs)
        val effects = model.getEffectsMap(context)
        with (holder) {
            outputView.text = outputNames[position]

            effectView.apply {
                val cv = 30 + position
                effectText.text = context.getString(R.string.label_sw2_effect_cv_x, cv)

                adapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    effects.values.toTypedArray()
                )

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val cvValue = effects.keys.elementAt(position)
                        model.setCvValue(cv, cvValue)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        // do nothing
                    }
                }

                model.liveCvValue(cv).observe(lifecycleOwner) {
                    if (effects.containsKey(it)) {
                        setSelection(effects.keys.indexOf(it))
                    }
                }
            }

            dayBrightnessView.apply {
                val cv = 46 + position
                dayRightnessText.text = context.getString(R.string.label_sw2_day_brightness_cv_x, cv)

                setOnChangeListener {
                    if (it !== null) {
                        model.setCvValue(cv, it)
                    }
                }
                model.liveCvValue(cv).observe(lifecycleOwner) { value = it }
            }

            nightBrightnessView.apply {
                val cv = 62 + position
                nightBrightnessText.text = context.getString(R.string.label_sw2_night_brightness_cv_x, cv)

                setOnChangeListener {
                    if (it !== null) {
                        model.setCvValue(cv, it)
                    }
                }
                model.liveCvValue(cv).observe(lifecycleOwner) { value = it }
            }

            fadeSpeedView.apply {
                val cv = 78 + position
                fadeSpeedText.text = context.getString(R.string.label_sw2_fade_speed_cv_x, cv)

                setOnChangeListener {
                    if (it !== null) {
                        model.setCvValue(cv, it)
                    }
                }
                model.liveCvValue(cv).observe(lifecycleOwner) { value = it }
            }
        }
    }
}