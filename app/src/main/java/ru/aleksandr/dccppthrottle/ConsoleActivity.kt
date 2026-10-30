/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.store.ConsoleStore
import ru.aleksandr.dccppthrottle.ui.console.ConsoleRecyclerViewAdapter

class ConsoleActivity : AwakeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_console)

        val listView = findViewById<RecyclerView>(R.id.listConsole)
        var followTail = savedInstanceState == null
        val adapter = ConsoleRecyclerViewAdapter {
            if (followTail && it > 0) listView.scrollToPosition(it - 1)
        }
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        listView.layoutManager = LinearLayoutManager(this)
        // Log updates should not animate every incoming command.
        listView.itemAnimator = null
        listView.adapter = adapter
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                followTail = !recyclerView.canScrollVertically(1)
            }
        })

        ConsoleStore.data.observe(this) { adapter.submitList(it) }
    }
}
