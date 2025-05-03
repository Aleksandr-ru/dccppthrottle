/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentXp5OutputsItemBinding

class Xp5OutputsRecyclerViewAdapter(
    private val model: Xp5OutputsViewModel
) : RecyclerView.Adapter<Xp5OutputsRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(binding: FragmentXp5OutputsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val outputView: TextView = binding.textOutput
        val effectAView: TextView = binding.textEffectA
        val effectBView: TextView = binding.textEffectB
        val pwmAView: TextView = binding.textPwmA
        val pwmBView: TextView = binding.textPwmB
        val flagsAView: TextView = binding.textFlagsA
        val flagsBView: TextView = binding.textFlagsB
        val param1AView: TextView = binding.textParameter1A
        val param1BView: TextView = binding.textParameter1B
        val param2AView: TextView = binding.textParameter2A
        val param2BView: TextView = binding.textParameter2B

        init {
           binding.root.setOnClickListener {
                model.editRow(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentXp5OutputsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = Xp5OutputsViewModel.ROWS

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val outputNames = context.resources.getStringArray(R.array.xp5_outputs)
        val effectNames = model.getEffectsMap(context)
        with (holder) {
            outputView.text = outputNames[position]

            effectAView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_EFFECTA).let {
                effectNames.getOrElse(it) { it.toString() }
            }
            pwmAView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_PWMA).toString()
            flagsAView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_FLAGSA).toString()
            param1AView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_PARAM1A).toString()
            param2AView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_PARAM2A).toString()

            effectBView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_EFFECTB).let {
                effectNames.getOrElse(it) { it.toString() }
            }
            pwmBView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_PWMB).toString()
            flagsBView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_FLAGSB).toString()
            param1BView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_PARAM1B).toString()
            param2BView.text = model.getCvValue(position, Xp5OutputsViewModel.COL_PARAM2B).toString()
        }
    }
}