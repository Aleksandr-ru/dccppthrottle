package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.LocomotivesStore

class LocomotiveDialog() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_locomotive, null)

            val pmAddr = view.findViewById<PlusMinusView>(R.id.plusminusAddr)
            val editTitle = view.findViewById<EditText>(R.id.editTextTitle)
            val dialogTitle = getString(
                if (storeIndex > -1) R.string.title_dialog_locomotive_edit
                else R.string.title_dialog_locomotive_add
            )

            val barMin = view.findViewById<SeekBar>(R.id.seekBarMin)
            val barMax = view.findViewById<SeekBar>(R.id.seekBarMax)
            val textMin = view.findViewById<TextView>(R.id.textViewMin)
            val textMax = view.findViewById<TextView>(R.id.textViewMax)

            barMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                    if (value < 1) bar?.progress = 1
                    else if (value > MAX_MIN_SPEED) bar?.progress = MAX_MIN_SPEED
                    else textMin.text = "$value%"

                    if (value >= barMax.progress) barMax.progress = value + 1
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    // "Required but not yet implemented"
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    // "Required but not yet implemented"
                }
            })

            barMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                    if (value < MIN_MAX_SPEED) bar?.progress = MIN_MAX_SPEED
                    else textMax.text = "$value%"

                    if (value <= barMin.progress) barMin.progress = value - 1
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    // "Required but not yet implemented"
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    // "Required but not yet implemented"
                }
            })

            val initial = LocomotivesStore.data.value?.getOrNull(storeIndex)
            pmAddr.isEnabled = initial == null || initial.slot == 0
            pmAddr.value = initial?.address
            editTitle.setText(initial?.title)
            if (initial != null) {
                barMin.progress = initial.minSpeed
                barMax.progress = initial.maxSpeed
            }

            builder.setView(view)
                .setTitle(dialogTitle)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    if (storeIndex > -1) {
                        with(initial!!) {
                            address = pmAddr.value!!
                            title = editTitle.text.toString().ifBlank { null }
                            minSpeed = barMin.progress
                            maxSpeed = barMax.progress
                        }
                        LocomotivesStore.replace(storeIndex, initial)
                    }
                    else {
                        val loco = LocomotivesStore.LocomotiveState(
                            pmAddr.value!!,
                            editTitle.text.toString().ifBlank { null },
                            barMin.progress,
                            barMax.progress
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
        const val MIN_MAX_SPEED = 10
        const val MAX_MIN_SPEED = 90
        var storeIndex = -1
    }
}