package ru.aleksandr.dccppthrottle.ui.routes

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.RouteEditorActivity
import ru.aleksandr.dccppthrottle.databinding.FragmentRouteListItemBinding
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
            holder.num.text = accessories.size.toString()
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentRouteListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val num: TextView = binding.itemNum
        val button: ImageButton = binding.imageButton

        init {
            itemView.setOnClickListener {
                RouteEditorActivity.start(it.context, bindingAdapterPosition)
            }

            button.setOnClickListener { btn ->
                // TODO run route
                val accessories = RoutesStore.data.value!![bindingAdapterPosition].accessories
                GlobalScope.launch {
                    accessories.forEach {
                        Toast.makeText(
                            itemView.context,
                            "Route acc ${it.address} set ${it.isOn}, delay ${it.delay}",
                            it.delay
                        ).show()
                        delay(it.delay.toLong() * 2) // TODO remove x2
                    }
                    // this.cancel()
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
                        RoutesStore.removeByIndex(bindingAdapterPosition)
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