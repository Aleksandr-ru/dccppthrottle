/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.routes

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.RouteEditorActivity
import ru.aleksandr.dccppthrottle.databinding.FragmentRouteListItemBinding
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.RoutesStore

class RoutesRecyclerViewAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RoutesRecyclerViewAdapter.ViewHolder>() {
    private var values: List<RoutesStore.RouteState> = listOf()

    fun replaceValues(newValues: List<RoutesStore.RouteState>) {
        values = newValues
        // notifyDataSetChanged()
        // Cannot call this method while RecyclerView is computing a layout or scrolling
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            FragmentRouteListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RoutesRecyclerViewAdapter.ViewHolder, position: Int) {
        with(values[position]) {
            holder.title.text = toString()
            holder.params.text = holder.itemView.context.getString(R.string.route_params, accessories.size)
            holder.button.isEnabled = accessories.isNotEmpty()
            holder.button.imageAlpha = if (accessories.isNotEmpty()) 255 else 75
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentRouteListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val params: TextView = binding.itemParams
        val button: ImageButton = binding.imageButton

        init {
            itemView.setOnClickListener {
                RouteEditorActivity.start(it.context, bindingAdapterPosition)
            }

            button.setOnClickListener { btn ->
                val title = RoutesStore.data.value!![bindingAdapterPosition].title
                val accessories = RoutesStore.data.value!![bindingAdapterPosition].accessories
                var job: Job? = null
                val dialog = ProgressDialog(itemView.context).apply {
                    setTitle(title)
                    setMax(accessories.size)
                    setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
                    show()
                }

                job = GlobalScope.launch {
                    accessories.forEach { acc ->
                        // java.lang.IllegalStateException: Cannot invoke setValue on a background thread
                        // https://stackoverflow.com/a/60126585
                        dialog.setMessage(acc.toString())
                        dialog.incrementProgress()
                        CommandStation.setAccessoryState(acc.address, acc.isOn)
                        delay(acc.delay.toLong())
                    }
                    dialog.dismiss()
                }
            }

            val popup = PopupMenu(itemView.context, itemView)
            popup.inflate(R.menu.list_item_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_context_edit -> {
                        RouteEditorActivity.start(itemView.context, bindingAdapterPosition)
                        true
                    }
                    R.id.action_context_delete -> {
                        RoutesStore.remove(bindingAdapterPosition)
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
    }
}