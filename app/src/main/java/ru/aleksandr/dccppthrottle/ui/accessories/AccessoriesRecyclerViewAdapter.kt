package ru.aleksandr.dccppthrottle.ui.accessories

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.databinding.FragmentAccessoryListItemBinding
import ru.aleksandr.dccppthrottle.store.AccessoriesStore

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
        with(values.get(position)) {
            holder.title.text = toString()
            holder.address.text = address.toString()
            holder.button.isChecked = state
            val p = position
            holder.button.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) AccessoriesStore.data.value?.get(p)?.let {
                    AccessoriesStore.replaceByIndex(
                        p,
                        AccessoriesStore.AccessoryState(
                            it.address,
                            List(20) { ('a'..'z').random() }.joinToString("")
                        )
                    )
                    // AccessoriesStore.setStateByAddress(it.address, isChecked)
                }
                AccessoriesStore.setStateByIndex(p, isChecked)

            }
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    inner class ViewHolder(binding: FragmentAccessoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val address: TextView = binding.itemAddr
        val button: ToggleButton = binding.toggleButton

        override fun toString(): String {
            return super.toString() + " '" + title.text + "'"
        }
    }
}