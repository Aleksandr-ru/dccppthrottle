/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.dialogs.PomBitDialog
import ru.aleksandr.dccppthrottle.dialogs.PomValueDialog
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.ui.cab.LocoCabViewPagerAdapter

class LocoCabActivity : AwakeActivity(),
    PomValueDialog.PomValueDialogListener,
    PomBitDialog.PomBitDialogListener {
    private var slot: Int = 0
    private var lastCv: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loco_cab)

        val slots = LocomotivesStore.getSlots()

        val adapter = LocoCabViewPagerAdapter(this, slots)
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                slot = slots[position]
                setTitleForSlot()
            }
        })

        slot = intent.getIntExtra(ARG_SLOT, 0)
        if (slot > 0) {
            viewPager.currentItem = slots.indexOf(slot)
            if (viewPager.currentItem == 0) setTitleForSlot()
        }
    }

    fun setTitleForSlot() {
        title = getString(R.string.title_activity_cab, slot)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.loco_cab, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_stop -> {
                Toast.makeText(this, R.string.message_stop_all, Toast.LENGTH_SHORT).show()
                CommandStation.emergencyStop()
                true
            }
            R.id.action_edit_loco -> {
                LocomotiveDialog.storeIndex = LocomotivesStore.getIndexBySlot(slot)
                LocomotiveDialog().show(supportFragmentManager, LocomotiveDialog.TAG)
                true
            }
            R.id.action_pom_value -> {
                PomValueDialog.cv = lastCv
                PomValueDialog().show(supportFragmentManager, PomValueDialog.TAG)
                true
            }
            R.id.action_pom_bit -> {
                PomBitDialog.cv = lastCv
                PomBitDialog().show(supportFragmentManager, PomBitDialog.TAG)
                true
            }
            R.id.action_console -> {
                val myIntent = Intent(this, ConsoleActivity::class.java)
                startActivity(myIntent)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onPomValueDialogResult(dialog: DialogFragment, cv: Int, value: Int) {
        val message = getString(R.string.message_write_cv_value, cv, value)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        CommandStation.setCvMain(slot, cv, value)
        lastCv = cv
    }

    override fun onPomBitDialogResult(dialog: DialogFragment, cv: Int, bit: Int, value: Int) {
        val message = getString(R.string.message_write_cv_bit, cv, bit, value)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        CommandStation.setCvBitMain(slot, cv, bit, value)
        lastCv = cv
    }

    companion object {
        const val ARG_SLOT = "slot"

        @JvmStatic
        fun start(context: Context, slot: Int) {
            val intent = Intent(context, LocoCabActivity::class.java)
            intent.putExtra(ARG_SLOT, slot)
            context.startActivity(intent)
        }
    }
}