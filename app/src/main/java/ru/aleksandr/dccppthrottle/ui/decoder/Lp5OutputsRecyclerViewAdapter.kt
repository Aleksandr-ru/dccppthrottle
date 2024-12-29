/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentLp5OutputsItemBinding

class Lp5OutputsRecyclerViewAdapter(
    private val model: Lp5OutputsViewModel
) : RecyclerView.Adapter<Lp5OutputsRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(binding: FragmentLp5OutputsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val outputView: TextView = binding.textOutput
        val modeView: TextView = binding.textMode
        val onOffDelayView: TextView = binding.textOnOffDelay
        val autoOffView: TextView = binding.textAutoOff
        val brightnessView: TextView = binding.textBrightness
        val special1View: TextView = binding.textSpecial1
        val special2View: TextView = binding.textSpecial2
        val special3View: TextView = binding.textSpecial3

        init {
           binding.root.setOnClickListener {
                model.editRow(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentLp5OutputsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return Lp5OutputsViewModel.ROWS
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val outputNames = context.resources.getStringArray(R.array.lp5_outputs)
        val modeNames = context.resources.getStringArray(R.array.lp5_output_modes)
        with (holder) {
            outputView.text = outputNames[position]
            modeView.text = model.getCvValue(position, Lp5OutputsViewModel.COL_MODE).let {
                if (it < modeNames.size) modeNames[it]
                else context.getString(R.string.label_unknown_x, it)
            }
            onOffDelayView.text = model.getCvValue(position, Lp5OutputsViewModel.COL_ONOFFDELAY).toString()
            autoOffView.text = model.getCvValue(position, Lp5OutputsViewModel.COL_AUTOOFF).toString()
            brightnessView.text = model.getCvValue(position, Lp5OutputsViewModel.COL_BRIGHTNESS).toString()
            special1View.text = model.getCvValue(position, Lp5OutputsViewModel.COL_SPECIAL1).toString()
            special2View.text = model.getCvValue(position, Lp5OutputsViewModel.COL_SPECIAL2).toString()
            special3View.text = model.getCvValue(position, Lp5OutputsViewModel.COL_SPECIAL3).toString()
        }
    }
}