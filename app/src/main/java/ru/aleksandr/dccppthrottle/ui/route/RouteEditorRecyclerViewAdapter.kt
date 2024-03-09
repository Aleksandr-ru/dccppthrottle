/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.route

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentRouteEditorItemBinding
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.RoutesStore
import java.util.*

class RouteEditorRecyclerViewAdapter(
    private val fragmentManager: FragmentManager,
    private val itemTouchHelper: ItemTouchHelper,
    private val subtitleFormat: String,
    private val routeIndex: Int
) : RecyclerView.Adapter<RouteEditorRecyclerViewAdapter.ViewHolder>() {
    private var values: List<RoutesStore.RouteStateAccessory> = listOf()

    fun replaceValues(newValues: List<RoutesStore.RouteStateAccessory>) {
        values = newValues
        // notifyDataSetChanged()
        // Cannot call this method while RecyclerView is computing a layout or scrolling
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            FragmentRouteEditorItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(values[position]) {
            holder.title.text = toString()
            holder.subtitle.text = String.format(subtitleFormat, address, delay)
            holder.toggle.isChecked = isOn
        }
    }

    override fun getItemCount(): Int = values.size

    fun moveItem(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(values, i, i + 1)
            }
        }
        else if (from > to) {
            for (i in from downTo to + 1) {
                Collections.swap(values, i, i - 1)
            }
        }
    }

    inner class ViewHolder(binding: FragmentRouteEditorItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val subtitle: TextView = binding.itemSubtitle
        val toggle: ToggleButton = binding.toggleButton
        val dragHandle: ImageView = binding.imageViewDrag

        init {
            dragHandle.setOnTouchListener { view, motionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(this)
                }
                else {
                    // onTouch lambda should call View#performClick when a click is detected
                    view.performClick()
                }
                true
            }

            toggle.setOnCheckedChangeListener { btn, isChecked ->
                if (btn.isPressed) {
                    RoutesStore.toggleAccessory(routeIndex, bindingAdapterPosition, isChecked)
                    notifyItemChanged(bindingAdapterPosition)
                }
            }

            val popup = PopupMenu(itemView.context, itemView)
            popup.inflate(R.menu.list_item_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_context_edit -> {
                        editAccessoryDialog(routeIndex, bindingAdapterPosition)
                        true
                    }
                    R.id.action_context_delete -> {
                        RoutesStore.removeAccessory(routeIndex, bindingAdapterPosition)
                        true
                    }
                    else -> false
                }
            }

            itemView.setOnLongClickListener {
                popup.show()
                true
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + title.text + "'"
        }

        private fun editAccessoryDialog(routeIndex: Int, accessoryIndex: Int) {
            val context = itemView.context
            val items = AccessoriesStore.data.value?.map { it.toString() }?.toTypedArray()
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.action_sel_acc))
                .setItems(items) { dialog, index ->
                    val addr = AccessoriesStore.getAddress(index)!!
                    val acc = RoutesStore.RouteStateAccessory(addr)
                    RoutesStore.replaceAccessory(routeIndex, accessoryIndex, acc)
                    dialog.dismiss()
                }
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }
}