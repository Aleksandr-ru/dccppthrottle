/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.AccessoryDialog
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.dialogs.RouteAddDialog
import ru.aleksandr.dccppthrottle.store.*
import ru.aleksandr.dccppthrottle.ui.main.MainViewPagerAdapter
import ru.aleksandr.dccppthrottle.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AwakeActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var viewPager: ViewPager2

    private var doubleBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val menuItem = navigationView.menu.findItem(R.id.power_switch_item)
        val powerSwitch = menuItem.actionView.findViewById<Switch>(R.id.power_switch)
        powerSwitch.setOnClickListener {
            CommandStation.setTrackPower(powerSwitch.isChecked)
        }
        MainStore.trackPower.observe(this) {
            powerSwitch.isChecked = it
        }

        val adapter = MainViewPagerAdapter(this)
        viewPager = binding.mainPager
        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                supportActionBar?.title = when(position){
                    POSITION_LOCOMOTIVES -> getString(R.string.title_fragment_locomotives)
                    POSITION_ACCESSORIES -> getString(R.string.title_fragment_accessories)
                    POSITION_ROUTES -> getString(R.string.title_fragment_routes)
                    else -> ""
                }
                MainStore.setViewPagerPosition(position)
            }
        })
        MainStore.viewPagerPosition.observe(this) {
            viewPager.currentItem = it
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        if (item.itemId != R.id.power_switch_item) {
            drawerLayout.closeDrawers()
        }
        return when (item.itemId) {
            R.id.nav_locomotives -> {
                viewPager.currentItem = POSITION_LOCOMOTIVES
                false
            }
            R.id.nav_accessories -> {
                viewPager.currentItem = POSITION_ACCESSORIES
                false
            }
            R.id.nav_routes -> {
                viewPager.currentItem = POSITION_ROUTES
                false
            }
            R.id.nav_programming -> {
                val myIntent = Intent(this, ProgActivity::class.java)
                startActivity(myIntent)
                false
            }
            R.id.nav_console -> {
                val myIntent = Intent(this, ConsoleActivity::class.java)
                startActivity(myIntent)
                false
            }
            R.id.nav_settings -> {
                val myIntent = Intent(this, SettingsActivity::class.java)
                startActivity(myIntent)
                false
            }
            R.id.nav_disconnect -> {
                doubleBack = true
                onBackPressed()
                false
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_stop -> {
                val slots = LocomotivesStore.getSlots()
                if (slots.isNotEmpty()) {
                    Toast.makeText(this, R.string.message_stop_all, Toast.LENGTH_SHORT).show()
                    CommandStation.emergencyStop()
                }
                true
            }
            R.id.action_add_loco -> {
                viewPager.currentItem = POSITION_LOCOMOTIVES
                LocomotiveDialog.storeIndex = -1
                LocomotiveDialog().show(supportFragmentManager, LocomotiveDialog.TAG)
                true
            }
            R.id.action_add_acc -> {
                viewPager.currentItem = POSITION_ACCESSORIES
                AccessoryDialog.storeIndex = -1
                AccessoryDialog().show(supportFragmentManager, AccessoryDialog.TAG)
                true
            }
            R.id.action_add_route -> {
                viewPager.currentItem = POSITION_ROUTES
                RouteAddDialog().show(supportFragmentManager, RouteAddDialog.TAG)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        if (drawerLayout.isOpen) {
            drawerLayout.closeDrawers()
        }
        if (doubleBack) {
            CommandStation.disconnect()

            val prefKeyConnectStartup = getString(R.string.pref_key_connect_startup)
            val prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
            prefsEditor.putBoolean(prefKeyConnectStartup, false)
            prefsEditor.commit()

            super.onBackPressed()
        }
        else {
            Toast.makeText(this, R.string.message_press_to_disconnect, Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBack = false
            }, MainStore.SHORT_DELAY)
            doubleBack = true
        }
    }

    override fun onPause() {
        super.onPause()

        try {
            saveStoreToFile(LocomotivesStore)
        }
        catch (e: Exception) {
            Toast.makeText(this, R.string.message_failed_save_locos, Toast.LENGTH_SHORT).show()
        }
        try {
            saveStoreToFile(AccessoriesStore)
        }
        catch (e: Exception) {
            Toast.makeText(this, R.string.message_failed_save_acc, Toast.LENGTH_SHORT).show()
        }
        try {
            saveStoreToFile(RoutesStore)
        }
        catch (e: Exception) {
            Toast.makeText(this, R.string.message_failed_save_routes, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStoreToFile(store: JsonStoreInterface) {
        if (store.hasUnsavedData) {
            val fileName = "${store.javaClass.simpleName}.json"
            val file = File(filesDir, fileName)
            val bufferedWriter = file.bufferedWriter()
            val jsonArray = store.toJson()
            bufferedWriter.use {
                it.write(jsonArray.toString())
            }
            store.hasUnsavedData = false
        }
    }

    companion object {
        const val POSITION_LOCOMOTIVES = 0
        const val POSITION_ACCESSORIES = 1
        const val POSITION_ROUTES = 2
    }
}