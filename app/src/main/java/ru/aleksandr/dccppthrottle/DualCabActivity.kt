/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.MainStore
import ru.aleksandr.dccppthrottle.ui.cab.CabViewPagerAdapter

class DualCabActivity : AwakeActivity() {

    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dual_cab)

        val slots = LocomotivesStore.getSlots()

        val activity = this
        val pagerLeft = findViewById<ViewPager2>(R.id.pagerLeft).apply {
            adapter = CabViewPagerAdapter(activity, R.layout.fragment_dual_cab_left, slots)
            MainStore.dualCabViewPagerPosition.value?.let {
                if (it.first < slots.size)
                    setCurrentItem(it.first, false)
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    MainStore.setDualCabPositionLeft(position)
                }
            })
        }

        val pagerRight = findViewById<ViewPager2>(R.id.pagerRight).apply {
            adapter = CabViewPagerAdapter(activity, R.layout.fragment_dual_cab_right, slots)
            MainStore.dualCabViewPagerPosition.value?.let {
                if (it.second < slots.size)
                    setCurrentItem(it.second, false)
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    MainStore.setDualCabPositionRight(position)
                }
            })
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            MainStore.dualCabViewPagerPosition.observe(this) {
                val locoLeft = LocomotivesStore.getBySlot(slots[it.first])
                title = if (it.first != it.second) {
                    val locoRight = LocomotivesStore.getBySlot(slots[it.second])
                    locoLeft.toString() + " + " + locoRight.toString()
                } else locoLeft.toString()
            }
        }

        val layout = findViewById<LinearLayout>(R.id.layoutDualCab)
        val snackbar = Snackbar.make(layout, R.string.message_track_off, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.action_turn_on) {
                val join = MainStore.trackPower.value?.second ?: false
                CommandStation.setTrackPower(true, join)
            }
        MainStore.trackPower.observe(this) {
            if (it.first) snackbar.dismiss()
            else if (slots.size > 1) snackbar.show()
        }

        if (slots.size < 2) {
            pagerLeft.visibility = View.GONE
            pagerRight.visibility = View.GONE
            findViewById<View>(R.id.divider)?.visibility = View.GONE
            findViewById<TextView>(R.id.empty_view)?.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    MainStore.setMainViewPagerPosition(MainActivity.POSITION_LOCOMOTIVES)
                    onBackPressed()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dual_cab, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_stop -> {
                Toast.makeText(this, R.string.message_stop_all, Toast.LENGTH_SHORT).show()
                CommandStation.emergencyStop()
                true
            }
            R.id.action_edit_loco_left -> {
                MainStore.dualCabViewPagerPosition.value?.let {
                    val slot = LocomotivesStore.getSlots()[it.first]
                    LocomotiveDialog.storeIndex = LocomotivesStore.getIndexBySlot(slot)
                    LocomotiveDialog().show(supportFragmentManager, LocomotiveDialog.TAG)
                }
                true
            }
            R.id.action_func_editor_left -> {
                MainStore.dualCabViewPagerPosition.value?.let {
                    val slot = LocomotivesStore.getSlots()[it.first]
                    FunctionsActivity.start(this, slot)
                }
                true
            }
            R.id.action_edit_loco_right -> {
                MainStore.dualCabViewPagerPosition.value?.let {
                    val slot = LocomotivesStore.getSlots()[it.second]
                    LocomotiveDialog.storeIndex = LocomotivesStore.getIndexBySlot(slot)
                    LocomotiveDialog().show(supportFragmentManager, LocomotiveDialog.TAG)
                }
                true
            }
            R.id.action_func_editor_right -> {
                MainStore.dualCabViewPagerPosition.value?.let {
                    val slot = LocomotivesStore.getSlots()[it.second]
                    FunctionsActivity.start(this, slot)
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context, leftPosition: Int? = null, rightPosition: Int? = null) {
            leftPosition?.let {
                MainStore.setDualCabPositionLeft(it)
            }
            rightPosition?.let {
                MainStore.setDualCabPositionRight(it)
            }
            val intent = Intent(context, DualCabActivity::class.java)
            context.startActivity(intent)
        }
    }
}