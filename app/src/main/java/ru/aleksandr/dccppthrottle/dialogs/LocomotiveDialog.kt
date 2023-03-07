package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore

class LocomotiveDialog () : DialogFragment() {

    private var dialogTitle: String? = null
    private var initial: LocomotivesStore.LocomotiveState? = null
    private var resultListener : ((LocomotivesStore.LocomotiveState) -> Boolean)? = null

    fun setTitle(title: String) {
        dialogTitle = title
    }

    fun setIntitial(item: LocomotivesStore.LocomotiveState) {
        initial = item
    }

    fun setListener(listener: (LocomotivesStore.LocomotiveState) -> Boolean) {
        resultListener = listener
    }

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
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    val loco = LocomotivesStore.LocomotiveState(
                        addr.value!!,
                        title.text.toString().ifBlank { null }
                    )
                    resultListener?.let {
                        if (it(loco)) dialog.dismiss()
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
    }
}