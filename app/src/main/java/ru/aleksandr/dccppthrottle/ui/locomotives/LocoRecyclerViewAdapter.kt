package ru.aleksandr.dccppthrottle.ui.locomotives

import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import ru.aleksandr.dccppthrottle.LocoCabActivity
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore.LocomotiveState
import ru.aleksandr.dccppthrottle.databinding.FragmentLocoListItemBinding as FragmentLocoBinding

class LocoRecyclerViewAdapter() : RecyclerView.Adapter<LocoRecyclerViewAdapter.ViewHolder>() {
    private var values: List<LocomotiveState> = listOf()

    fun replaceValues(newValues: List<LocomotiveState>) {
        values = newValues
        // notifyDataSetChanged()
        // Cannot call this method while RecyclerView is computing a layout or scrolling
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentLocoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        if (item.slot > 0) {
            holder.itemView.alpha = 1F
            holder.switch.isChecked = true
            holder.switch.isEnabled = true
            holder.slot.text = "#" + item.slot
        }
        else {
            holder.itemView.alpha = 0.5F
            holder.switch.isChecked = false
            holder.switch.isEnabled = LocomotivesStore.hasFreeSlots()
            holder.slot.text = ""
        }
        holder.address.text = item.address.toString()
        holder.title.text = item.toString()
        holder.progress.progress = item.speed
        if (item.speed > 0) {
            if (item.reverse) {
                holder.direction.text = "R ${item.speed}"
            }
            else {
                holder.direction.text = "F ${item.speed}"
            }
        }
        else {
            holder.direction.text = "STOP"
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentLocoBinding) : RecyclerView.ViewHolder(binding.root) {
        val switch: Switch = binding.swAssigned
        val slot: TextView = binding.itemSlot
        val title: TextView = binding.itemTitle
        val address: TextView = binding.itemAddr
        val progress: ProgressBar = binding.progressBar
        val direction: TextView = binding.itemDir

        init {
            itemView.setOnClickListener {
                val slot = LocomotivesStore.getSlotByIndex(bindingAdapterPosition)
                if (slot > 0) LocoCabActivity.start(it.context, slot)
            }

            val popup = PopupMenu(itemView.context, itemView)
            popup.inflate(R.menu.context_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_context_edit -> {
                        Toast.makeText(itemView.context, "Edit $bindingAdapterPosition", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_context_delete -> {
                        Toast.makeText(itemView.context, "Delete $bindingAdapterPosition", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            itemView.setOnLongClickListener {
                popup.show()
                true
            }

            switch.setOnCheckedChangeListener { sw, isChecked ->
                if (sw.isPressed) {
                    if (isChecked) {
                        val slot = LocomotivesStore.getAvailableSlot()
                        if (slot > 0) LocomotivesStore.assignToSlot(bindingAdapterPosition, slot)
                        else Toast.makeText(sw.context, "No slots available", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        LocomotivesStore.assignToSlot(bindingAdapterPosition, 0)
                    }
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + title.text + "'"
        }
    }

}