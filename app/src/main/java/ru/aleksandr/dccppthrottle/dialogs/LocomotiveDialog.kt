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

            addr.value = initial?.address
            title.setText(initial?.title)

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    val loco = LocomotivesStore.LocomotiveState(
                        addr.value!!,
                        title.text.toString().ifBlank { null }
                    )
                    if (storeIndex > -1) {
                        if (loco.slot > 0) {
                            CommandStation.stopLocomotive(loco.slot)
                            // todo unassign loco from cs
                            LocomotivesStore.assignToSlot(storeIndex, 0)
                        }
                        loco.slot = 0
                        LocomotivesStore.replace(storeIndex, loco)
                    }
                    else {
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