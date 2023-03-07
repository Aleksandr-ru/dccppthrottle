package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.AccessoriesStore

class PomBitDialog() : DialogFragment() {

    private lateinit var dialog: AlertDialog
    private var cv: Int? = null
    private var selectedBit: Int? = null
    private var resultListener : ((cv: Int, bit: Int, value: Int) -> Boolean)? = null

    fun setCv(cvNum: Int) {
        cv = cvNum
    }

    fun setListener(listener: (cv: Int, bit: Int, value: Int) -> Boolean) {
        resultListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //return super.onCreateDialog(savedInstanceState)
        val dialogTitle = getString(R.string.title_dialog_pom_bit)
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_pom_bit, null)

            val viewCv = view.findViewById<PlusMinusView>(R.id.plusminusCvNum)
            viewCv.value = cv

            val viewRadioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
            val viewBits = Array<RadioButton>(8) { i ->
                val bit = layoutInflater.inflate(R.layout.bit_radio, viewRadioGroup, false) as RadioButton
                viewRadioGroup.addView(bit, 0)
                bit.apply {
                    text = i.toString()
                    tag = i
                    setOnClickListener {
                        selectedBit = i
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }

            val viewValue = view.findViewById<ToggleButton>(R.id.toggleBitValue)

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(R.string.label_write) { dialog, _ ->
                    val newCv = viewCv.value!!
                    val newValue = if (viewValue.isChecked) 1 else 0
                    resultListener?.let {
                        if (it(newCv, selectedBit!!, newValue)) dialog.dismiss()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            dialog = builder.create()
            dialog
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }
}