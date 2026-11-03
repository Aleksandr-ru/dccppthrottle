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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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

        private val observations = mutableListOf<Pair<LiveData<Int>, Observer<Int>>>()
        private var updatingViews = false
        var effectSelection = AdapterView.INVALID_POSITION

        fun observeCv(cv: Int, render: (Int) -> Unit) {
            fun refresh() {
                updatingViews = true
                try {
                    // A committed-value emission may arrive after an edited-value emission.
                    render(model.getCvValue(cv))
                } finally {
                    updatingViews = false
                }
            }
            refresh()
            val liveValue = model.liveCvValue(cv)
            val observer = Observer<Int> { refresh() }
            observations.add(liveValue to observer)
            liveValue.observe(lifecycleOwner, observer)
        }

        fun bindNumber(input: PlusMinusView, cv: Int) {
            observeCv(cv) { input.value = it }
            input.setOnChangeListener { value ->
                if (!updatingViews && value != null) model.setCvValue(cv, value)
            }
        }

        fun unbind() {
            observations.forEach { (liveValue, observer) -> liveValue.removeObserver(observer) }
            observations.clear()
            effectView.onItemSelectedListener = null
            dayBrightnessView.setOnChangeListener(null)
            nightBrightnessView.setOnChangeListener(null)
            fadeSpeedView.setOnChangeListener(null)
            effectSelection = AdapterView.INVALID_POSITION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentSw2OutputsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = 13

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.unbind()
        val context = holder.itemView.context
        val outputNames = context.resources.getStringArray(R.array.sw2_outputs)
        val effects = model.getEffectsMap(context)
        val effectValues = effects.keys.toList()
        with(holder) {
            outputView.text = outputNames[position]

            val effectCv = 30 + position
            effectText.text = context.getString(R.string.label_sw2_effect_cv_x, effectCv)
            effectView.adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                effects.values.toTypedArray()
            )
            observeCv(effectCv) {
                effectSelection = effectValues.indexOf(it)
                effectView.setSelection(effectSelection)
            }
            effectView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Spinner also reports selections made while rendering or rebinding.
                    if (position != effectView.selectedItemPosition || position == effectSelection) return
                    val cvValue = effectValues.getOrNull(position) ?: return
                    effectSelection = position
                    model.setCvValue(effectCv, cvValue)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

            val dayCv = 46 + position
            dayRightnessText.text = context.getString(R.string.label_sw2_day_brightness_cv_x, dayCv)
            bindNumber(dayBrightnessView, dayCv)

            val nightCv = 62 + position
            nightBrightnessText.text = context.getString(R.string.label_sw2_night_brightness_cv_x, nightCv)
            bindNumber(nightBrightnessView, nightCv)

            val fadeCv = 78 + position
            fadeSpeedText.text = context.getString(R.string.label_sw2_fade_speed_cv_x, fadeCv)
            bindNumber(fadeSpeedView, fadeCv)
        }
    }
}
