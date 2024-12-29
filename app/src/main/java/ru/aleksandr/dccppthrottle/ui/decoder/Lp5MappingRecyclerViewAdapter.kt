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
import ru.aleksandr.dccppthrottle.databinding.FragmentLp5MappingItemBinding
import kotlin.math.pow

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

            binding.root.setOnLongClickListener {
                model.reloadRow(bindingAdapterPosition)
                true
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
        val context = holder.itemView.context
        with (holder) {
            numberView.text = String.format("%02d", position + 1)
            inputsView.apply {
                setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Body1)
                text = controlCVsToString(context, position, model.inputColumnIndexes).ifEmpty {
                    setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Small)
                    itemView.context.getString(R.string.placeholder_no_value)
                }
            }
            outputsView.apply {
                setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Body1)
                text = controlCVsToString(context, position, model.outputColumnIndexes).ifEmpty {
                    setTextAppearance(androidx.appcompat.R.style.Base_TextAppearance_AppCompat_Small)
                    itemView.context.getString(R.string.placeholder_no_value)
                }
            }
        }
    }

    private fun controlCVsToString(context: Context, rowIndex: Int, colRange: IntRange): String {
        val result = mutableListOf<String>()
        for (ci in colRange) {
            val cvValue = model.getCvValue(rowIndex, ci)
            val stringArrayId = Lp5MappingViewModel.CONTROL_CV_STRING_ID[ci]
            val stringList = context.resources.getStringArray(stringArrayId).toList()
            result += controlCvValueToStrings(cvValue, stringList)
        }
        return result.joinToString(", ")
    }

    private fun controlCvValueToStrings(value: Int, strings: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (i in strings.indices) {
            val ii = 2f.pow(i).toInt()
            if (value and ii == ii) result.add(strings[i])
        }
        return result.toList()
    }
}