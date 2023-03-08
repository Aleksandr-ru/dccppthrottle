package ru.aleksandr.dccppthrottle.ui.routes

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import ru.aleksandr.dccppthrottle.cs.CommandStation
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
            holder.button.isEnabled = accessories.isNotEmpty()
            holder.button.imageAlpha = if (accessories.isNotEmpty()) 255 else 75
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
                val route = RoutesStore.data.value!![bindingAdapterPosition]
                val accessories = RoutesStore.data.value!![bindingAdapterPosition].accessories
                val progressView = ProgressBar(
                    itemView.context,
                    null,
                    android.R.attr.progressBarStyleHorizontal
                ).apply {
                    max = accessories.size
                    val padding = resources.getDimension(R.dimen.dialog_padding)
                    setPadding(padding.toInt())
                }
                var job: Job? = null
                val dialog = AlertDialog.Builder(itemView.context)
                    .setTitle(route.title)
                    .setView(progressView)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        job?.cancel()
                    }.show()

                job = GlobalScope.launch {
                    accessories.forEach { acc ->
                        // java.lang.IllegalStateException: Cannot invoke setValue on a background thread
                        // https://stackoverflow.com/a/60126585
                        CommandStation.setAccessoryState(acc.address, acc.isOn)
                        progressView.incrementProgressBy(1)
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