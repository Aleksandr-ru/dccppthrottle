/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.ui.functions.FunctionsRecyclerViewAdapter

class FunctionsActivity : AwakeActivity() {

    private val TAG = javaClass.simpleName
    private var slot: Int = 0
    private lateinit var swNamed: Switch
    private lateinit var adapter: FunctionsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_functions)

        slot = intent.getIntExtra(ARG_SLOT, 0)
        val loco = LocomotivesStore.getBySlot(slot)!!

        val titleView = findViewById<TextView>(R.id.textViewTitle)
        titleView.text = loco.toString()

        swNamed = findViewById<Switch>(R.id.swNamed)
        swNamed.isChecked = loco.namedOnly

        adapter = FunctionsRecyclerViewAdapter(loco.funcNames, loco.funcReset)

        val listView = findViewById<RecyclerView>(R.id.rvFunctions)
        listView.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        LocomotivesStore.setAllFuncNamesBySlot(slot, adapter.getNameValues())
        LocomotivesStore.setAllFuncResetBySlot(slot, adapter.getResetValues())
        LocomotivesStore.setNamedOnlyBySlot(slot, swNamed.isChecked)
    }

    override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
        super.onBackPressed()
    }

    companion object {
        const val ARG_SLOT = "slot"

        @JvmStatic
        fun start(context: Context, slot: Int) {
            val intent = Intent(context, FunctionsActivity::class.java)
            intent.putExtra(ARG_SLOT, slot)
            context.startActivity(intent)
        }
    }
}