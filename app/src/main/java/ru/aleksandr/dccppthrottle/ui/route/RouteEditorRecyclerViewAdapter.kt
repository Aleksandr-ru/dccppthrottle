package ru.aleksandr.dccppthrottle.ui.route

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.databinding.FragmentRouteEditorItemBinding
import ru.aleksandr.dccppthrottle.dialogs.RouteAccEditDialog
import ru.aleksandr.dccppthrottle.store.RoutesStore
import java.util.*

class RouteEditorRecyclerViewAdapter(
    private val fragmentManager: FragmentManager,
    private val itemTouchHelper: ItemTouchHelper,
    private val subtitleFormat: String,
    private val routeIndex: Int
) : RecyclerView.Adapter<RouteEditorRecyclerViewAdapter.ViewHolder>() {
    private var values: List<RoutesStore.RouteStateAccessory> = listOf()

    fun replaceValues(newValues: List<RoutesStore.RouteStateAccessory>) {
        values = newValues
        // notifyDataSetChanged()
        // Cannot call this method while RecyclerView is computing a layout or scrolling
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            FragmentRouteEditorItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(values[position]) {
            holder.title.text = toString()
            holder.subtitle.text = String.format(subtitleFormat, address, delay)
            holder.toggle.isChecked = isOn
        }
    }

    override fun getItemCount(): Int = values.size

    fun moveItem(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(values, i, i + 1)
            }
        }
        else if (from > to) {
            for (i in from downTo to + 1) {
                Collections.swap(values, i, i - 1)
            }
        }
    }

    inner class ViewHolder(binding: FragmentRouteEditorItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val subtitle: TextView = binding.itemSubtitle
        val toggle: ToggleButton = binding.toggleButton
        val dragHandle: ImageView = binding.imageViewDrag

        init {
            dragHandle.setOnTouchListener { view, motionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(this)
                }
                else {
                    // onTouch lambda should call View#performClick when a click is detected
                    view.performClick()
                }
                true
            }

            toggle.setOnCheckedChangeListener { btn, isChecked ->
                if (btn.isPressed) {
//                    val routeAccessory = values[bindingAdapterPosition]
//                    routeAccessory.isOn = isChecked
//                    RoutesStore.replaceAccessory(routeIndex, bindingAdapterPosition, routeAccessory)
                    RoutesStore.toggleAccessory(routeIndex, bindingAdapterPosition, isChecked)
                    notifyItemChanged(bindingAdapterPosition)
                }
            }

            val popup = PopupMenu(itemView.context, itemView)
            popup.inflate(R.menu.list_item_menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_context_edit -> {
                        RouteAccEditDialog.routeIndex = routeIndex
                        RouteAccEditDialog.accessoryIndex = bindingAdapterPosition
                        RouteAccEditDialog().show(fragmentManager, RouteAccEditDialog.TAG)
                        true
                    }
                    R.id.action_context_delete -> {
                        RoutesStore.removeAccessory(routeIndex, bindingAdapterPosition)
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