package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R

class PomBitDialog (
    private val cv: Int,
    private val resultListener : (cv: Int, bit: Int, value: Int) -> Boolean,
) : DialogFragment() {

    private lateinit var dialog: AlertDialog
    private var selectedBit: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //return super.onCreateDialog(savedInstanceState)
        val dialogTitle = getString(R.string.title_dialog_pom_bit)
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_pom_bit, null)

            val viewCv = view.findViewById<PlusMinusView>(R.id.plusminusCvNum)
            viewCv.value = cv

            val viewGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
            val viewBits = Array<RadioButton>(8) { i ->
                RadioButton(
                    context,
                    null,
                    0,
                    R.style.Widget_Theme_DCCppThrottle_Radio_TextOnBottom
                ).apply {
                    text = i.toString()
                    tag = i
                    setOnClickListener {
                        selectedBit = i
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
            viewBits.forEach { viewGroup.addView(it, 0) }
//            for (i in 7..0) {
//                val viewBit = RadioButton(context, null, R.style.Widget_Theme_DCCppThrottle_Radio_TextOnBottom).apply {
//                    text = i.toString()
//                    tag = i
//                    setOnClickListener {
//                        selectedBit = i
//                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
//                    }
//                }
//                viewGroup.addView(viewBit, 0)
//            }
            val viewValue = view.findViewById<ToggleButton>(R.id.toggleBitValue)

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(R.string.label_write) { dialog, _ ->
                    val newCv = viewCv.value!!
                    val newValue = if (viewValue.isChecked) 1 else 0
                    if (resultListener(newCv, selectedBit!!, newValue)) {
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(R.string.label_cancel) { dialog, _ ->
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