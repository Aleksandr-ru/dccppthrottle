package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.PomBitDialog
import ru.aleksandr.dccppthrottle.dialogs.PomValueDialog
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.ui.cab.LocoCabViewPagerAdapter

class LocoCabActivity : AppCompatActivity() {
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
                CommandStation.stopLocomotive(slot)
                true
            }
            R.id.action_pom_value -> {
                PomValueDialog().apply {
                    setCv(lastCv)
                    setListener { cv, value ->
                        lastCv = cv
                        CommandStation.setCvMain(slot, cv, value)
                        true
                    }
                }.show(supportFragmentManager, "pom_val")
                true
            }
            R.id.action_pom_bit -> {
                PomBitDialog().apply {
                    setCv(lastCv)
                    setListener { cv, bit, value ->
                        lastCv = cv
                        CommandStation.setCvBitMain(slot, cv, bit, value)
                        true
                    }
                }.show(supportFragmentManager, "pom_bit")
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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