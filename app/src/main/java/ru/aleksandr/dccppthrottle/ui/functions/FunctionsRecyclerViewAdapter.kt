/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.functions

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentFunctionItemBinding

class FunctionsRecyclerViewAdapter(
    private val values: Array<String>
) : RecyclerView.Adapter<FunctionsRecyclerViewAdapter.ViewHolder>() {

    private val TAG = javaClass.simpleName

    fun getValues() = values

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FunctionsRecyclerViewAdapter.ViewHolder {
        return ViewHolder(
            FragmentFunctionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FunctionsRecyclerViewAdapter.ViewHolder, position: Int) {
        holder.label.text = holder.label.context.getString(R.string.label_f, position)
        with(holder.edit) {
            hint = holder.label.text
            setText(values[position])
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentFunctionItemBinding): RecyclerView.ViewHolder(binding.root) {
        val label: TextView = binding.itemF
        val edit: EditText = binding.itemName

        init {
            edit.doAfterTextChanged {
                values[bindingAdapterPosition] = it.toString()
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + label.text + "'"
        }
    }
}