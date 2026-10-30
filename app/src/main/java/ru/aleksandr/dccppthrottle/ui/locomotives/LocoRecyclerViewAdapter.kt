/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.locomotives

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.LocoCabActivity
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.databinding.FragmentLocoListItemBinding as FragmentLocoBinding

internal object LocoListDiff : DiffUtil.ItemCallback<LocoListItem>() {
    override fun areItemsTheSame(oldItem: LocoListItem, newItem: LocoListItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: LocoListItem, newItem: LocoListItem) = oldItem == newItem
    override fun getChangePayload(oldItem: LocoListItem, newItem: LocoListItem) = newItem.changesFrom(oldItem)
}

internal class LocoRecyclerViewAdapter(
    private val fragmentManager: FragmentManager
) : ListAdapter<LocoListItem, LocoRecyclerViewAdapter.ViewHolder>(LocoListDiff) {
    init {
        setHasStableIds(true)
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FragmentLocoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), LocoListItem.ALL)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty() || payloads.any { it !is Int }) {
            onBindViewHolder(holder, position)
        } else {
            holder.bind(getItem(position), payloads.fold(0) { changes, payload -> changes or (payload as Int) })
        }
    }

    inner class ViewHolder(private val binding: FragmentLocoBinding) : RecyclerView.ViewHolder(binding.root) {
        private var boundId: Long = RecyclerView.NO_ID
        private var popupTargetId: Long = RecyclerView.NO_ID

        fun bind(item: LocoListItem, changes: Int) {
            boundId = item.id
            if (changes and LocoListItem.DETAILS != 0) {
                binding.itemTitle.text = item.title
                binding.itemAddr.text = itemView.context.getString(R.string.dcc_addr, item.address)
            }
            if (changes and LocoListItem.ASSIGNMENT != 0) {
                itemView.alpha = if (item.slot > 0) 1F else 0.5F
                binding.swAssigned.isChecked = item.slot > 0
                binding.swAssigned.isEnabled = item.assignmentEnabled
                binding.itemSlot.text = if (item.slot > 0) "#${item.slot}" else ""
            }
            if (changes and LocoListItem.MOVEMENT != 0) {
                binding.progressBar.progress = item.progress
                binding.itemDir.text = itemView.context.getString(
                    when {
                        item.speed <= 0 -> R.string.speed_stop
                        item.reverse -> R.string.speed_rev
                        else -> R.string.speed_fwd
                    }, item.speed
                )
            }
        }

        init {
            itemView.setOnClickListener {
                val index = LocomotivesStore.getIndexById(boundId)
                if (index >= 0) {
                    val slot = LocomotivesStore.getSlot(index)
                    if (slot > 0) LocoCabActivity.start(it.context, slot)
                }
            }

            val popup = PopupMenu(itemView.context, itemView)
            popup.inflate(R.menu.list_item_menu)
            popup.setOnMenuItemClickListener { menuItem ->
                val index = LocomotivesStore.getIndexById(popupTargetId)
                if (index < 0) return@setOnMenuItemClickListener false
                when (menuItem.itemId) {
                    R.id.action_context_edit -> {
                        LocomotiveDialog.storeIndex = index
                        LocomotiveDialog().show(fragmentManager, LocomotiveDialog.TAG)
                        true
                    }
                    R.id.action_context_delete -> {
                        val slot = LocomotivesStore.getSlot(index)
                        if (slot > 0) {
                            CommandStation.stopLocomotive(slot)
                            CommandStation.unassignLoco(slot)
                            LocomotivesStore.assignToSlot(index, 0)
                        }
                        val currentIndex = LocomotivesStore.getIndexById(popupTargetId)
                        if (currentIndex >= 0) LocomotivesStore.remove(currentIndex)
                        true
                    }
                    else -> false
                }
            }
            itemView.setOnLongClickListener {
                popupTargetId = boundId
                popup.show()
                true
            }

            // A programmatic rebind must never send commands, including while pressed.
            binding.swAssigned.setOnClickListener {
                val index = LocomotivesStore.getIndexById(boundId)
                if (index < 0) return@setOnClickListener
                if (binding.swAssigned.isChecked) {
                    try {
                        val slot = LocomotivesStore.assignToSlot(index)
                        CommandStation.stopLocomotive(slot)
                    } catch (ex: LocomotivesStore.LocomotiveNoSlotsAvailableException) {
                        Toast.makeText(it.context, R.string.message_no_slots, Toast.LENGTH_SHORT).show()
                        binding.swAssigned.isChecked = false
                    } catch (ex: LocomotivesStore.LocomotiveAddressInUseException) {
                        Toast.makeText(it.context, R.string.message_address_in_use, Toast.LENGTH_SHORT).show()
                        binding.swAssigned.isChecked = false
                    }
                } else {
                    val slot = LocomotivesStore.getSlot(index)
                    CommandStation.stopLocomotive(slot)
                    CommandStation.unassignLoco(slot)
                    LocomotivesStore.assignToSlot(index, 0)
                }
            }
        }
    }
}
