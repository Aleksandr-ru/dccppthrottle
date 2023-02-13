package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.AccessoriesStore


class AccessoryDialog (
    private val dialogTitle: String,
    private val initial: AccessoriesStore.AccessoryState?,
    private val resultListener : (AccessoriesStore.AccessoryState) -> Boolean,
) : DialogFragment() {

    private lateinit var dialog : AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //return super.onCreateDialog(savedInstanceState)
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_locomotive, null)

            val addr = view.findViewById<PlusMinusView>(R.id.plusminusAddr)
            val title = view.findViewById<EditText>(R.id.editTextTitle)

            addr.value = initial?.address
            addr.onChangeListener = {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    if (it == null) false
                    else !AccessoriesStore.hasAddress(it!!)
            }
            title.setText(initial?.title)

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(R.string.label_ok) { dialog, id ->
                    val acc = AccessoriesStore.AccessoryState(
                        addr.value!!,
                        title.text.toString().ifBlank { null }
                    )
                    if (resultListener(acc)) {
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(R.string.label_cancel) { dialog, id ->
                    dialog.cancel()
                }
            dialog = builder.create()
            dialog
        }
    }

    override fun onResume() {
        super.onResume()
        val addr = dialog.findViewById<PlusMinusView>(R.id.plusminusAddr)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
            if (addr.value == null) false
            else !AccessoriesStore.hasAddress(addr.value!!)
    }
}