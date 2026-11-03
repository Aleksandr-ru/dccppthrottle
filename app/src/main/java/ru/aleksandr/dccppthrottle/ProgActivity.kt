/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.store.MainStore
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ru.aleksandr.dccppthrottle.ui.prog.ProgPagerAdapter

class ProgActivity : AwakeActivity() {
    @Suppress("unused")
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prog)
        val pager = findViewById<ViewPager2>(R.id.progPager)
        pager.adapter = ProgPagerAdapter(this)
        val titles = listOf(R.string.title_fragment_prog_cv, R.string.title_fragment_prog_speed_curve,
            R.string.title_fragment_prog_extended_curve)
        TabLayoutMediator(findViewById<TabLayout>(R.id.progTabs), pager) { tab, position ->
            tab.setText(titles[position])
        }.attach()
        pager.setCurrentItem(savedInstanceState?.getInt("programmingTab") ?: 0, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("programmingTab", findViewById<ViewPager2>(R.id.progPager).currentItem)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.prog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Back button pressed
                super.onOptionsItemSelected(item)
            }
            else -> {
                DecoderActivity.start(this, item.itemId)
                true
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // If you drive onto a programming track that is “joined” and enter a programming command,
        // the track will automatically switch to a programming track.
        // If you use a compatible Throttle, you can then send the join command again
        // and drive off the track onto the rest of your layout!
        // @see https://dcc-ex.com/reference/software/command-summary-consolidated.html#onoff-track-turn-power-on-or-off-to-the-main-and-prog-tracks
        if (!isChangingConfigurations && MainStore.trackPower.value == Pair(true, true)) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Restoring track join")
            CommandStation.setTrackPower(isOn = true, join = true)
        }
    }
}
