package ru.aleksandr.dccppthrottle.ui.accessories

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentAccessoryListItemBinding
import ru.aleksandr.dccppthrottle.store.AccessoriesStore

class AccessoriesRecyclerViewAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<AccessoriesRecyclerViewAdapter.ViewHolder>() {
    private var values: List<AccessoriesStore.AccessoryState> = listOf()

    fun replaceValues(newValues: List<AccessoriesStore.AccessoryState>) {
        values = newValues
        // notifyDataSetChanged()
        // Cannot call this method while RecyclerView is computing a layout or scrolling
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            FragmentAccessoryListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AccessoriesRecyclerViewAdapter.ViewHolder, position: Int) {
        val strAddr = holder.itemView.context.getString(R.string.accessory_params)
        with(values[position]) {
            holder.title.text = toString()
            holder.address.text = String.format(strAddr, address)
            holder.button.isChecked = isOn
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentAccessoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val address: TextView = binding.itemAddr
        val button: ToggleButton = binding.toggleButton

        init {
            button.setOnCheckedChangeListener { _, isChecked ->
                AccessoriesStore.setStateByIndex(bindingAdapterPosition, isChecked)
            }

            val popup = PopupMenu(itemView.context, itemView)
            popup.inflate(R.menu.list_item_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_context_edit -> {
                        Toast.makeText(itemView.context, "Edit $bindingAdapterPosition", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_context_delete -> {
                        // TODO check in routes
                        AccessoriesStore.removeByIndex(bindingAdapterPosition)
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