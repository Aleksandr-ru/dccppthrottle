package ru.aleksandr.dccppthrottle.ui.locomotives

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.LocoCabActivity
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore.LocomotiveState
import ru.aleksandr.dccppthrottle.databinding.FragmentLocoListItemBinding as FragmentLocoBinding

class LocoRecyclerViewAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<LocoRecyclerViewAdapter.ViewHolder>() {
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
        holder.title.text = item.toString()
        holder.progress.progress = item.speed
        holder.address.text = holder.itemView.context.getString(R.string.dcc_addr, item.address)

        if (item.speed > 0) {
            if (item.reverse) {
                holder.direction.text = holder.itemView.context.getString(R.string.speed_rev, item.speed)
            }
            else {
                holder.direction.text = holder.itemView.context.getString(R.string.speed_fwd, item.speed)
            }
        }
        else {
            holder.direction.text = holder.itemView.context.getString(R.string.speed_stop)
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
                val slot = LocomotivesStore.getSlot(bindingAdapterPosition)
                if (slot > 0) LocoCabActivity.start(it.context, slot)
            }

            val popup = PopupMenu(itemView.context, itemView)
            popup.inflate(R.menu.list_item_menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_context_edit -> {
                        LocomotiveDialog.storeIndex = bindingAdapterPosition
                        LocomotiveDialog().show(fragmentManager, LocomotiveDialog.TAG)
                        true
                    }
                    R.id.action_context_delete -> {
                        val slot = LocomotivesStore.getSlot(bindingAdapterPosition)
                        if (slot > 0) {
                            CommandStation.stopLocomotive(slot)
                            CommandStation.unassignLoco(slot)
                            LocomotivesStore.assignToSlot(bindingAdapterPosition, 0)
                        }
                        LocomotivesStore.remove(bindingAdapterPosition)
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
                        try {
                            val slot = LocomotivesStore.assignToSlot(bindingAdapterPosition)
                            CommandStation.stopLocomotive(slot)
                        }
                        catch (ex : LocomotivesStore.LocomotiveNoSlotsAvailableException) {
                            Toast.makeText(sw.context, R.string.message_no_slots, Toast.LENGTH_SHORT).show()
                            sw.isChecked = false
                        }
                        catch (ex : LocomotivesStore.LocomotiveAddressInUseException) {
                            Toast.makeText(sw.context, R.string.message_address_in_use, Toast.LENGTH_SHORT).show()
                            sw.isChecked = false
                        }
                    }
                    else {
                        val slot = LocomotivesStore.getSlot(bindingAdapterPosition)
                        CommandStation.stopLocomotive(slot)
                        CommandStation.unassignLoco(slot)
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