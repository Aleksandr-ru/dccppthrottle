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
import ru.aleksandr.dccppthrottle.ui.prog.ProgFragment

class ProgActivity : AwakeActivity() {
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prog)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProgFragment.newInstance())
                .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.prog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // BAck button pressed
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
        if (MainStore.trackPower.value == true && MainStore.trackJoin.value == true) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Restoring track join")
            CommandStation.setTrackPower(isOn = true, join = true)
        }
    }
}