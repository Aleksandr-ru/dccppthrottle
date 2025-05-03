/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class Xp4OutputDimmingFragment() : Fragment() {
    private val TAG = javaClass.simpleName
    private val model by activityViewModels<Xp4OutputsViewModel>()

    private val layoutId = R.layout.fragment_xp4_output_dimming

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val table1 = view.findViewById<TableLayout>(R.id.tableLayout)
        val table2 = view.findViewById<TableLayout>(R.id.tableLayout2)
        resources.getStringArray(R.array.xp4_outputs).withIndex().forEach { a ->
            addTableRow(table1, a.value, a.index + 116)
            addTableRow(table2, a.value, a.index + 150)
        }
    }

    private fun addTableRow(table: TableLayout, name: String, cv: Int) {
        val v =
            layoutInflater.inflate(R.layout.fragment_xp4_output_dimming_row, table, false)
        v.findViewById<TextView>(R.id.textViewName).apply {
            text = name
        }
        v.findViewById<PlusMinusView>(R.id.plusminusValue).apply {
            setOnChangeListener {
                if (it !== null) model.setCvValue(cv, it)
            }
            model.liveCvValue(cv).observe(viewLifecycleOwner) { value = it }
        }
        table.addView(v)
    }
}