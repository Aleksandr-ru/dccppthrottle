package ru.aleksandr.dccppthrottle.ui.locomotives

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import ru.aleksandr.dccppthrottle.LocoCabActivity
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.placeholder.PlaceholderContent.PlaceholderItem
import kotlin.math.abs
import ru.aleksandr.dccppthrottle.databinding.FragmentLocoListItemBinding as FragmentLocoBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class LocoRecyclerViewAdapter(
    private val values: List<PlaceholderItem>/*,
    private val listener: (PlaceholderItem) -> Unit*/
) : RecyclerView.Adapter<LocoRecyclerViewAdapter.ViewHolder>() {

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
        holder.idView.text = item.slot.toString()
        holder.contentView.text = item.toString()
        holder.progress.progress = abs(item.speed.toInt())
        holder.address.text = item.addr.toString()
        if (item.speed.toInt() < 0) {
            holder.direction.text = "R " + abs(item.speed.toInt()).toString()
        }
        else if (item.speed.toInt() > 0) {
            holder.direction.text = "F ${item.speed}"
        }
        else
            holder.direction.text = "STOP"

        //holder.itemView.setOnClickListener { listener(item) }
        holder.itemView.setOnClickListener {
//            val myIntent = Intent(it.context, LocoCabActivity::class.java)
//            myIntent.putExtra("slot", item.slot)
//            it.context.startActivity(myIntent)
            LocoCabActivity.start(it.context, item.slot.toInt())
        }

        val popup = PopupMenu(holder.itemView.context, holder.itemView)
        popup.inflate(R.menu.context_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_context_edit -> {
                    Toast.makeText(holder.itemView.context, "Edit $item", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_context_delete -> {
                    Toast.makeText(holder.itemView.context, "Delete $item", Toast.LENGTH_SHORT).show()
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

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentLocoBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content
        val progress: ProgressBar = binding.progressBar
        val address: TextView = binding.itemAddr
        val direction: TextView = binding.itemDir

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}