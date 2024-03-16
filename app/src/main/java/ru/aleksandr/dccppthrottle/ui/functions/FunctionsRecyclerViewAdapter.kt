/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.functions

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentFunctionItemBinding
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class FunctionsRecyclerViewAdapter(
    private val nameValues: Array<String>,
    private val resetValues: IntArray
) : RecyclerView.Adapter<FunctionsRecyclerViewAdapter.ViewHolder>() {

    private val TAG = javaClass.simpleName

    fun getNameValues() = nameValues

    fun getResetValues() = resetValues

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
            setText(nameValues[position])
        }
        with(holder.reset) {
            value = resetValues[position]
        }
    }

    override fun getItemCount(): Int = nameValues.size

    inner class ViewHolder(binding: FragmentFunctionItemBinding): RecyclerView.ViewHolder(binding.root) {
        val label: TextView = binding.itemF
        val edit: EditText = binding.itemName
        val reset: PlusMinusView = binding.plusminusReset

        init {
            edit.doAfterTextChanged {
                nameValues[bindingAdapterPosition] = it.toString()
            }

            reset.setOnChangeListener {
                resetValues[bindingAdapterPosition] = it ?: 0
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + label.text + "'"
        }
    }
}