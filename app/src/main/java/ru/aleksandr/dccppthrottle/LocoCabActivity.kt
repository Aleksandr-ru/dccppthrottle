package ru.aleksandr.dccppthrottle

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import ru.aleksandr.dccppthrottle.placeholder.PlaceholderContent
import ru.aleksandr.dccppthrottle.ui.cab.LocoCabViewPagerAdapter

private const val ARG_SLOT = "slot"

class LocoCabActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loco_cab)

        val adapter = LocoCabViewPagerAdapter(this, PlaceholderContent.ITEMS)
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                title = "Slot #${position+1}"
            }
        })

        val slot = intent.getIntExtra(ARG_SLOT, 0)
        if (slot > 0) viewPager.currentItem = slot - 1
    }

    companion object {
        @JvmStatic
        fun start(context: Context, slot: Int) {
            val intent = Intent(context, LocoCabActivity::class.java)
            intent.putExtra(ARG_SLOT, slot)
            context.startActivity(intent)
        }
    }
}