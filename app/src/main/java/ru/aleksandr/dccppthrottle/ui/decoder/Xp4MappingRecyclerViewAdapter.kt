/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentXp4MappingItemBinding

class Xp4MappingRecyclerViewAdapter(
    private val model: Xp4MappingViewModel
) : RecyclerView.Adapter<Xp4MappingRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(binding: FragmentXp4MappingItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val numberView: TextView = binding.textNum
        val inputsView: TextView = binding.textInputs
        val outputsView: TextView = binding.textOutputs

        init {
           binding.root.setOnClickListener {
                model.editRow(bindingAdapterPosition)
            }

            binding.root.setOnLongClickListener {
                model.reloadRow(bindingAdapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentXp4MappingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return Xp4MappingViewModel.ROWS
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        with (holder) {
            numberView.text = String.format("%02d", position + 1)
            inputsView.apply {
                setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Body1)
                text = mappingCVsToString(context, position, model.inputColumnIndexes).ifEmpty {
                    setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Small)
                    itemView.context.getString(R.string.placeholder_no_value)
                }
            }
            outputsView.apply {
                setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Body1)
                text = mappingCVsToString(context, position, model.outputColumnIndexes).ifEmpty {
                    setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Small)
                    itemView.context.getString(R.string.placeholder_no_value)
                }
            }
        }
    }

    private fun mappingCVsToString(context: Context, rowIndex: Int, colRange: IntRange): String {
        val result = mutableListOf<String>()
        val stringList = context.resources.getStringArray(R.array.xp4_mapping_bits).toList()
        for (ci in colRange) {
            val cvValue = model.getCvValue(rowIndex, ci)
            val byteList = stringList.subList(ci * 8, ci * 8 + 8)
            result += mappingCvValueToStrings(cvValue, byteList)
        }
        return result.joinToString(", ")
    }

    private fun mappingCvValueToStrings(value: Int, strings: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (i in strings.indices) {
            val ii = 1 shl i
            if (value and ii == ii) result.add(strings[i])
        }
        return result.toList()
    }
}