package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.LocomotivesStore

class PomValueDialog() : DialogFragment() {

    private var cv: Int = 0
    private var resultListener : ((cv: Int, value: Int) -> Boolean)? = null

    fun setCv(cvNum: Int) {
        cv = cvNum
    }

    fun setListener(listener: (cv: Int, value: Int) -> Boolean) {
        resultListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //return super.onCreateDialog(savedInstanceState)
        val dialogTitle = getString(R.string.title_dialog_pom_value)
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_pom_value, null)

            val viewCv = view.findViewById<PlusMinusView>(R.id.plusminusCvNum)
            val viewValue = view.findViewById<PlusMinusView>(R.id.plusminusCvValue)

            viewCv.value = cv

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(R.string.label_write) { dialog, _ ->
                    val newCv = viewCv.value!!
                    val newValue = viewValue.value!!
                    resultListener?.let {
                        if (it(newCv, newValue)) dialog.dismiss()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        }
    }
}