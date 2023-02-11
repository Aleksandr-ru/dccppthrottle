package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.MockStore

class LocomotiveDialog (
    private val dialogTitle: String,
    private val initial: LocomotivesStore.LocomotiveState?,
    private val resultListener : (LocomotivesStore.LocomotiveState) -> Boolean,
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //return super.onCreateDialog(savedInstanceState)
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_locomotive, null)

            val addr = view.findViewById<PlusMinusView>(R.id.plusminusAddr)
            val title = view.findViewById<EditText>(R.id.editTextTitle)

            addr.value = initial?.address
            title.setText(initial?.title)

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(R.string.label_ok) { dialog, id ->
                    val loco = LocomotivesStore.LocomotiveState(
                        addr.value!!,
                        title.text.toString().ifBlank { null }
                    )
                    if (resultListener(loco)) {
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(R.string.label_cancel) { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        }
    }
}