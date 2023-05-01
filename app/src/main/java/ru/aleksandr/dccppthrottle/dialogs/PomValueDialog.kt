/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R

class PomValueDialog() : DialogFragment() {

    private lateinit var listener: PomValueDialogListener

    interface PomValueDialogListener {
        fun onPomValueDialogResult(dialog: DialogFragment, cv: Int, value: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as PomValueDialogListener
        }
        catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement PomValueDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_pom_value, null)

            val viewCv = view.findViewById<PlusMinusView>(R.id.plusminusCvNum)
            val viewValue = view.findViewById<PlusMinusView>(R.id.plusminusCvValue)

            viewCv.value = cv
            viewValue.value = valuesMap[cv]

            viewCv.setOnChangeListener { cv ->
                if (valuesMap.containsKey(cv)) {
                    viewValue.value = valuesMap[cv]
                }
            }

            builder.setView(view)
                .setTitle(R.string.title_dialog_pom_value)
                .setCancelable(true)
                .setPositiveButton(R.string.label_write) { dialog, _ ->
                    val newCv = viewCv.value!!
                    val newValue = viewValue.value!!
                    listener.onPomValueDialogResult(this, newCv, newValue)
                    valuesMap[newCv] = newValue
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        }
    }

    companion object {
        const val TAG = "PomValueDialog"
        private val valuesMap: MutableMap<Int, Int> = mutableMapOf()
        var cv = 0
    }
}