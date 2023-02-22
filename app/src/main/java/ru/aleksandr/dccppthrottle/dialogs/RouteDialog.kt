package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.RoutesStore

class RouteDialog() : DialogFragment() {

    private var dialogTitle: String? = null
    private var resultListener : ((RoutesStore.RouteState) -> Boolean)? = null

    private lateinit var dialog : AlertDialog

    fun setTitle(title: String) {
        dialogTitle = title
    }

    fun setListener(listener: (RoutesStore.RouteState) -> Boolean) {
        resultListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //return super.onCreateDialog(savedInstanceState)
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_route, null)

            val title = view.findViewById<EditText>(R.id.editTextTitle)
            title.doAfterTextChanged {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !it.isNullOrBlank()
            }

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(R.string.label_ok) { dialog, id ->
                    if (!title.text.isNullOrBlank()) {
                        val route = RoutesStore.RouteState(title.text.toString())
                        resultListener?.let {
                            if (it(route)) dialog.dismiss()
                        }
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }
}