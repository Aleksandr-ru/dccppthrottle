/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

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
    private val idx = Xp4OutputsViewModel.IDX_DIMMING

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false).apply {
        findViewById<TextView>(R.id.textViewTitle)?.text =
            resources.getStringArray(R.array.xp4_output_titles)[idx]
        findViewById<TextView>(R.id.textViewDescription)?.text =
            resources.getStringArray(R.array.xp4_output_description)[idx]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val table = view.findViewById<TableLayout>(R.id.tableLayout)
        resources.getStringArray(R.array.xp4_outputs).withIndex().forEach { a ->
            val v =
                layoutInflater.inflate(R.layout.fragment_xp4_output_dimming_row, table, false)
            v.findViewById<TextView>(R.id.textViewName).apply {
                text = a.value
            }
            v.findViewById<PlusMinusView>(R.id.plusminusValue).apply {
                model.loaded.observe(viewLifecycleOwner) {
                    if (it) value = model.getCvValue(a.index + 116)
                }
                setOnChangeListener {
                    if (it !== null) model.setCvValue(a.index + 116, it)
                }
            }
            table.addView(v)
        }
    }
}