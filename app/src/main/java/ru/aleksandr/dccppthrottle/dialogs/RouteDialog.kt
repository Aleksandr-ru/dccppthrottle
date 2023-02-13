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
import ru.aleksandr.dccppthrottle.store.RoutesStore

class RouteDialog (
    private val dialogTitle: String,
    private val resultListener : (RoutesStore.RouteState) -> Boolean,
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //return super.onCreateDialog(savedInstanceState)
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_route, null)

            val title = view.findViewById<EditText>(R.id.editTextTitle)

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(R.string.label_ok) { dialog, id ->
                    if (!title.text.isNullOrBlank()) {
                        val route = RoutesStore.RouteState(title.text.toString())
                        if (resultListener(route)) {
                            dialog.dismiss()
                        }
                    }
                }
                .setNegativeButton(R.string.label_cancel) { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        }
    }
}