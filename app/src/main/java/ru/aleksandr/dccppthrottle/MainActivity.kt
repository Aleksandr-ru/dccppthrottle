package ru.aleksandr.dccppthrottle

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.dialogs.AccessoryDialog
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.dialogs.RouteDialog
import ru.aleksandr.dccppthrottle.store.*
import ru.aleksandr.dccppthrottle.ui.main.MainViewPagerAdapter
import ru.aleksandr.dccppthrottle.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var viewPager: ViewPager2

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
                MainStore.setPosition(position)
            }
        })
        MainStore.position.observe(this) {
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
        drawerLayout.closeDrawers()
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
                Toast.makeText(this,"Programming", Toast.LENGTH_SHORT).show()
                false
            }
            R.id.nav_console -> {
                Toast.makeText(this,"Console", Toast.LENGTH_SHORT).show()
                false
            }
            R.id.nav_settings -> {
                val myIntent = Intent(this, SettingsActivity::class.java)
                startActivity(myIntent)
                false
            }
            R.id.nav_disconnect -> {
                val myIntent = Intent(this, ConnectActivity::class.java)
                startActivity(myIntent)
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
//                    CommandStation.emergencyStop()
                }
                slots.forEach {
                    CommandStation.stopLocomotive(it)
                }
                true
            }
            R.id.action_add_loco -> {
                viewPager.currentItem = POSITION_LOCOMOTIVES
                val loco = LocomotivesStore.LocomotiveState(3)
                LocomotiveDialog(getString(R.string.title_dialog_locomotive_add), loco) {
                    LocomotivesStore.add(it)
                    true
                }.show(supportFragmentManager, "loco")
                true
            }
            R.id.action_add_acc -> {
                viewPager.currentItem = POSITION_ACCESSORIES
                val accessory = AccessoriesStore.AccessoryState(1)
                AccessoryDialog(getString(R.string.title_dialog_accessory_add), accessory) {
                    try {
                        AccessoriesStore.add(it)
                    }
                    catch (ex : AccessoriesStore.AccessoryAddressInUseException) {
                        Toast.makeText(this, "Address already in use", Toast.LENGTH_SHORT).show()
                    }
                    true
                }.show(supportFragmentManager, "accessory")
                true
            }
            R.id.action_add_route -> {
                viewPager.currentItem = POSITION_ROUTES
                RouteDialog(getString(R.string.title_dialog_route_add)) {
                    RoutesStore.add(it)
                    true
                }.show(supportFragmentManager, "route")
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        const val POSITION_LOCOMOTIVES = 0
        const val POSITION_ACCESSORIES = 1
        const val POSITION_ROUTES = 2
    }
}