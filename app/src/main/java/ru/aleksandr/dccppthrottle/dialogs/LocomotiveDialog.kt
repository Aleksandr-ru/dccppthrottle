package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore

class LocomotiveDialog() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_locomotive, null)

            val addr = view.findViewById<PlusMinusView>(R.id.plusminusAddr)
            val title = view.findViewById<EditText>(R.id.editTextTitle)
            val dialogTitle = getString(
                if (storeIndex > -1) R.string.title_dialog_locomotive_edit
                else R.string.title_dialog_locomotive_add
            )
            val initial = LocomotivesStore.data.value?.getOrNull(storeIndex)
            addr.isEnabled = initial == null || initial.slot == 0
            addr.value = initial?.address
            title.setText(initial?.title)

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    if (storeIndex > -1) {
                        initial!!.address = addr.value!!
                        initial!!.title = title.text.toString().ifBlank { null }
                        LocomotivesStore.replace(storeIndex, initial)
                    }
                    else {
                        val loco = LocomotivesStore.LocomotiveState(
                            addr.value!!,
                            title.text.toString().ifBlank { null }
                        )
                        LocomotivesStore.add(loco)
                    }
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        }
    }

    companion object {
        const val TAG = "LocomotiveDialog"
        var storeIndex = -1
    }
}