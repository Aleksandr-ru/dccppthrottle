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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.RoutesStore

class RouteAddDialog() : DialogFragment() {

    private lateinit var dialog : AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_route, null)

            val title = view.findViewById<EditText>(R.id.editTextTitle)
            title.doAfterTextChanged {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !it.isNullOrBlank()
            }

            builder.setView(view)
                .setTitle(R.string.title_dialog_route_add)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    if (!title.text.isNullOrBlank()) {
                        val route = RoutesStore.RouteState(title.text.toString())
                        RoutesStore.add(route)
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }

    companion object {
        const val TAG = "RouteDialog"
    }
}