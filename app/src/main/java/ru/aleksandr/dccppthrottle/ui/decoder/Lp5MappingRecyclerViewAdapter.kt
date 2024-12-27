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
import ru.aleksandr.dccppthrottle.databinding.FragmentLp5MappingItemBinding

class Lp5MappingRecyclerViewAdapter(
    private val model: Lp5MappingViewModel
) : RecyclerView.Adapter<Lp5MappingRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(binding: FragmentLp5MappingItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val numberView: TextView = binding.itemNum
        val inputsView: TextView = binding.itemInputs
        val outputsView: TextView = binding.itemOutputs

        init {
           binding.root.setOnClickListener {
                model.editRow(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentLp5MappingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return Lp5MappingViewModel.ROWS
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with (holder) {
            numberView.text = String.format("%02d", position + 1)
            inputsView.apply {
                setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Body1)
                text = model.inputsString(itemView.context, position).ifEmpty {
                    setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Small)
                    itemView.context.getString(R.string.placeholder_no_value)
                }
            }
            outputsView.apply {
                setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Body1)
                text = model.outputsString(itemView.context, position).ifEmpty {
                    setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Small)
                    itemView.context.getString(R.string.placeholder_no_value)
                }
            }
        }

    }
}