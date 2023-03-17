/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.AccessoriesStore

class AccessoryDialog () : DialogFragment() {

    private lateinit var dialog : AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_locomotive, null)

            val addr = view.findViewById<PlusMinusView>(R.id.plusminusAddr)
            val title = view.findViewById<EditText>(R.id.editTextTitle)
            val dialogTitle = getString(
                if (storeIndex > -1) R.string.title_dialog_accessory_edit
                else R.string.title_dialog_accessory_add
            )
            val initial = AccessoriesStore.data.value?.getOrNull(storeIndex)

            addr.value = initial?.address
            addr.setOnChangeListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    if (it == null) false
                    else !AccessoriesStore.hasAddress(it, storeIndex)
            }
            title.setText(initial?.title)

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    val acc = AccessoriesStore.AccessoryState(
                        addr.value!!,
                        title.text.toString().ifBlank { null }
                    )
                    try {
                        if (storeIndex > -1)
                            AccessoriesStore.replace(storeIndex, acc)
                        else
                            AccessoriesStore.add(acc)
                    }
                    catch (ex : AccessoriesStore.AccessoryAddressInUseException) {
                        Toast.makeText(context, R.string.message_address_in_use, Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { dialog, id ->
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
            else !AccessoriesStore.hasAddress(addr.value!!, storeIndex)
    }

    companion object {
        const val TAG = "AccessoryDialog"
        var storeIndex = -1
    }
}