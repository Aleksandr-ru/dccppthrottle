package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.MockStore
import ru.aleksandr.dccppthrottle.store.RoutesStore
import ru.aleksandr.dccppthrottle.ui.route.RouteEditorRecyclerViewAdapter

class RouteEditorActivity : AwakeActivity() {
    var routeIndex: Int = 0

    // https://yfujiki.medium.com/drag-and-reorder-recyclerview-items-in-a-user-friendly-manner-1282335141e9
    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object: ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapter = recyclerView.adapter as RouteEditorRecyclerViewAdapter
                    val from = viewHolder.bindingAdapterPosition
                    val to = target.bindingAdapterPosition
                    adapter.moveItem(from, to)
                    adapter.notifyItemMoved(from, to)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // 4. Code block for horizontal swipe.
                    //    ItemTouchHelper handles horizontal swipe as well, but
                    //    it is not relevant with reordering. Ignoring here.
                }

                override fun isLongPressDragEnabled(): Boolean {
                    return false
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                    if (actionState == ACTION_STATE_DRAG) {
                        viewHolder?.itemView?.alpha = 0.5f
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewHolder?.itemView?.alpha = 1.0f
                }
            }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_editor)

        routeIndex = intent.getIntExtra(ARG_ROUTE, 0)
        val editTextView = findViewById<EditText>(R.id.editTextName)
        val listView = findViewById<RecyclerView>(R.id.list_route_accessories)
        val placeholder = findViewById<TextView>(R.id.empty_view)

        editTextView.setText(RoutesStore.data.value!![routeIndex].title)
        editTextView.doAfterTextChanged {
            RoutesStore.setTitle(routeIndex, it.toString())
        }

        itemTouchHelper.attachToRecyclerView(listView)

        val adapter = RouteEditorRecyclerViewAdapter(
            supportFragmentManager,
            itemTouchHelper,
            getString(R.string.route_accessory_params),
            routeIndex
        )
        RoutesStore.liveAccessories(routeIndex).observe(this) {
            adapter.replaceValues(it)
            adapter.notifyDataSetChanged()

            if (it.isEmpty()) {
                listView.visibility = View.GONE
                placeholder.visibility = View.VISIBLE
            }
            else {
                listView.visibility = View.VISIBLE
                placeholder.visibility = View.GONE
            }
        }
        listView.adapter = adapter

        placeholder.setOnClickListener {
            if (BuildConfig.DEBUG) for(i in 1..10)
                RoutesStore.addAccessory(routeIndex, MockStore.randomRouteAccessory())
            else
                addAccessoryDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.route_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_add -> {
                addAccessoryDialog()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun addAccessoryDialog() {
        val items = AccessoriesStore.data.value?.map { it.toString() }?.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.action_add_acc))
            .setItems(items) { dialog, index ->
                val addr = AccessoriesStore.data.value!![index].address
                val acc = RoutesStore.RouteStateAccessory(addr)
                RoutesStore.addAccessory(routeIndex, acc)
                dialog.dismiss()
            }
            .setCancelable(true)
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    companion object {
        const val ARG_ROUTE = "route"

        @JvmStatic
        fun start(context: Context, routeIndex: Int) {
            val intent = Intent(context, RouteEditorActivity::class.java)
            intent.putExtra(ARG_ROUTE, routeIndex)
            context.startActivity(intent)
        }
    }
}