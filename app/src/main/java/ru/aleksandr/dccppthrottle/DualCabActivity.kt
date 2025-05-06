/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

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
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.MainStore
import ru.aleksandr.dccppthrottle.ui.cab.LocoCabViewPagerAdapter

class DualCabActivity : AwakeActivity() {

    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dual_cab)

        val slots = LocomotivesStore.getSlots()

        val activity = this
        val pagerLeft = findViewById<ViewPager2>(R.id.pagerLeft).apply {
            adapter = LocoCabViewPagerAdapter(activity, R.layout.fragment_dual_cab, slots)

//            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//                override fun onPageSelected(position: Int) {
//                    super.onPageSelected(position)
//                    MainStore.setCabViewPagerPosition(position)
//                }
//            })
        }

        val pagerRight = findViewById<ViewPager2>(R.id.pagerRight).apply {
            adapter = LocoCabViewPagerAdapter(activity, R.layout.fragment_dual_cab, slots)

//            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//                override fun onPageSelected(position: Int) {
//                    super.onPageSelected(position)
//                    MainStore.setCabViewPagerPosition(position)
//                }
//            })
        }

//        MainStore.cabViewPagerPosition.observe(this) {
//            viewPager.currentItem = it
//            slot = slots[it]
//
//            val loco = LocomotivesStore.getBySlot(slot)
//            title = getString(R.string.title_activity_cab, slot, loco.toString())
//        }

        val layout = findViewById<LinearLayout>(R.id.layoutDualCab)
        val snackbar = Snackbar.make(layout, R.string.message_track_off, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.action_turn_on) {
                CommandStation.setTrackPower(true, MainStore.trackJoin.value ?: false)
            }
        MainStore.trackPower.observe(this) {
            if (it) snackbar.dismiss()
            else if (slots.size > 1) snackbar.show()
        }

        if (slots.size < 2) {
            pagerLeft.visibility = View.GONE
            pagerRight.visibility = View.GONE
            findViewById<View>(R.id.divider)?.visibility = View.GONE
            findViewById<TextView>(R.id.empty_view)?.visibility = View.VISIBLE
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
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

//    companion object {
//        @JvmStatic
//        fun start(context: Context, slot: Int) {
//            val intent = Intent(context, DualCabActivity::class.java)
//            context.startActivity(intent)
//        }
//    }
}