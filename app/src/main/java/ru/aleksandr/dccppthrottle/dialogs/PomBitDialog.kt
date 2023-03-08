package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R

class PomBitDialog() : DialogFragment() {

    private var selectedBit: Int? = null

    private lateinit var dialog: AlertDialog
    private lateinit var listener: PomBitDialogListener

    interface PomBitDialogListener {
        fun onPomBitDialogResult(dialog: DialogFragment, cv: Int, bit: Int, value: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as PomBitDialogListener
        }
        catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement PomBitDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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
                .setTitle(R.string.title_dialog_pom_bit)
                .setCancelable(true)
                .setPositiveButton(R.string.label_write) { dialog, _ ->
                    val newCv = viewCv.value!!
                    val newValue = if (viewValue.isChecked) 1 else 0
                    listener.onPomBitDialogResult(this, newCv, selectedBit!!, newValue)
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

    companion object {
        const val TAG = "PomBitDialog"
        var cv = 0
    }
}