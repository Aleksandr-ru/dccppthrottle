package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.viewpager2.widget.ViewPager2
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.ui.cab.LocoCabViewPagerAdapter

class LocoCabActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loco_cab)

        val adapter = LocoCabViewPagerAdapter(this)
        LocomotivesStore.data.observe(this) {
            adapter.replaceValues(it.filter { item -> item.address > 0 })
            adapter.notifyDataSetChanged()
        }

        val viewPager = findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val slots = LocomotivesStore.getTakenSlots()
                title = getString(R.string.title_activity_cab) + slots[position]?.slot
            }
        })

        val slot = intent.getIntExtra(ARG_SLOT, 0)
        if (slot > 0) {
            viewPager.currentItem =
                LocomotivesStore.getTakenSlots().withIndex().find { it.value.slot == slot }?.index
                    ?: throw Exception("Index not found")
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