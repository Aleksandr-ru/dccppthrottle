package ru.aleksandr.dccppthrottle.ui.accessories

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentAccessoryListItemBinding
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.MockStore

class AccessoriesRecyclerViewAdapter : RecyclerView.Adapter<AccessoriesRecyclerViewAdapter.ViewHolder>() {
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
        with(values[position]) {
            holder.title.text = toString()
            holder.address.text = address.toString()
            holder.button.isChecked = state

            val p = position

            holder.button.setOnCheckedChangeListener { _, isChecked ->
                AccessoriesStore.setStateByIndex(p, isChecked)
            }

            val popup = PopupMenu(holder.itemView.context, holder.itemView)
            popup.inflate(R.menu.context_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_context_edit -> {
                        Toast.makeText(holder.itemView.context, "Edit $p", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_context_delete -> {
                        AccessoriesStore.removeByIndex(p)
                        true
                    }
                    else -> false
                }
            }

            holder.itemView.setOnLongClickListener {
                popup.show()
                true
            }
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentAccessoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val address: TextView = binding.itemAddr
        val button: ToggleButton = binding.toggleButton

        override fun toString(): String {
            return super.toString() + " '" + title.text + "'"
        }
    }
}