/*
 * Copyright (c) 2026. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.console

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.store.ConsoleStore

class ConsoleRecyclerViewAdapter(
    private val onListChanged: (Int) -> Unit
) : ListAdapter<ConsoleStore.Line, ConsoleRecyclerViewAdapter.ViewHolder>(LineDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val padding = (8 * context.resources.displayMetrics.density).toInt()
        val textView = LayoutInflater.from(context)
            .inflate(R.layout.fragment_console_item, parent, false) as TextView
        return ViewHolder(textView.apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, padding, padding, padding)
            setHorizontallyScrolling(false)
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val line = getItem(position)
        val outgoing = line.tag == ConsoleStore.TAG_OUT
        holder.textView.apply {
            text = line.str
            setTextColor(ContextCompat.getColor(context,
                if (outgoing) R.color.console_outgoing
                else R.color.console_incoming
            ))
            setCompoundDrawablesWithIntrinsicBounds(
                if (outgoing) R.drawable.ic_arrow_north_east_24
                else R.drawable.ic_arrow_south_west_24,
                0, 0, 0
            )
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<ConsoleStore.Line>,
        currentList: MutableList<ConsoleStore.Line>
    ) {
        onListChanged(currentList.size)
    }

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    private object LineDiff : DiffUtil.ItemCallback<ConsoleStore.Line>() {
        override fun areItemsTheSame(oldItem: ConsoleStore.Line, newItem: ConsoleStore.Line) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ConsoleStore.Line, newItem: ConsoleStore.Line) =
            oldItem == newItem
    }
}
