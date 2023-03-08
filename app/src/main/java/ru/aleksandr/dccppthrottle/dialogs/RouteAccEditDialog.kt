package ru.aleksandr.dccppthrottle.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import ru.aleksandr.dccppthrottle.view.PlusMinusView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.RoutesStore


class RouteAccEditDialog() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_route_accessory, null)

            val delay = view.findViewById<PlusMinusView>(R.id.plusminusDelay)
            val list = view.findViewById<Spinner>(R.id.spinnerAccList)
            val initial = RoutesStore.data.value?.getOrNull(routeIndex)?.accessories?.getOrNull(accessoryIndex)
            val accessoryNames = AccessoriesStore.data.value!!.map { it.toString() }
            val selectedIndex = initial?.let { acc -> AccessoriesStore.getIndexByAddress(acc.address) } ?: 0
            val adapter: ArrayAdapter<String> =
                ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, accessoryNames)
            list.adapter = adapter
            list.setSelection(selectedIndex)
            delay.value = initial?.delay ?: 0

            builder.setView(view)
                .setTitle(R.string.title_dialog_accessory_edit)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    val address: Int = AccessoriesStore.getAddress(list.selectedItemPosition)!!
                    val acc = RoutesStore.RouteStateAccessory(
                        address,
                        delay.value!!
                    )
                    RoutesStore.replaceAccessory(routeIndex, accessoryIndex, acc)
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        }
    }

    companion object {
        const val TAG = "RouteAccessoryDialog"
        var routeIndex = -1
        var accessoryIndex = -1
    }
}