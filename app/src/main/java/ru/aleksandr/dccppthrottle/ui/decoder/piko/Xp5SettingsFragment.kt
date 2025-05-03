/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.ui.decoder.DecoderFragment
import ru.aleksandr.dccppthrottle.ui.decoder.WipFragment


class Xp5SettingsFragment : DecoderFragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Xp5SettingsViewModel>()

    private lateinit var emptyView: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var writeButton: Button

    companion object {
        fun newInstance() = Xp5SettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_decoder_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            emptyView = findViewById(R.id.empty_view)
            viewPager = findViewById(R.id.pager)
            tabLayout = findViewById(R.id.tabLayout)
            writeButton = findViewById(R.id.buttonWrite)
        }
        
        viewPager.adapter = SlidePagerAdapter(this)

        val tabTitles = resources.getStringArray(R.array.xp5_conf_tabs)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles.get(position)
        }.attach()

        emptyView.setOnClickListener {
            readModelCVs(model, MANUFACTURER_ID_PIKO)
        }

        writeButton.setOnClickListener {
            val job = writeChangedCVs(model)
            job.invokeOnCompletion {
                if (!job.isCancelled) activity?.onBackPressed()
            }
        }

        model.hasChanges.observe(viewLifecycleOwner) {
            writeButton.isEnabled = it
        }

        model.loaded.observe(viewLifecycleOwner) {
            if (it) {
                emptyView.visibility = View.GONE
                tabLayout.visibility = View.VISIBLE
                viewPager.visibility = View.VISIBLE
                writeButton.visibility = View.VISIBLE
            }
            else {
                emptyView.visibility = View.VISIBLE
                tabLayout.visibility = View.GONE
                viewPager.visibility = View.GONE
                writeButton.visibility = View.GONE
            }
        }

        if ((savedInstanceState == null) && (model.loaded.value == true)) {
            model.discardChanges()
            Snackbar.make(view, R.string.message_cvs_outdated, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_reload) {
                    readModelCVs(model, MANUFACTURER_ID_PIKO)
                }.show()
        }
    }

    private inner class SlidePagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = resources.getStringArray(R.array.xp5_conf_tabs).size

        override fun createFragment(position: Int) = when(position) {
            Xp5SettingsViewModel.IDX_CONF -> Xp5SettingConfFragment()
            Xp5SettingsViewModel.IDX_FADING -> Xp5SettingFadingFragment()
            Xp5SettingsViewModel.IDX_FLASHING -> Xp5SettingFlashingFragment()
            Xp5SettingsViewModel.IDX_LAMPS -> Xp5SettingLampsFragment()
            Xp5SettingsViewModel.IDX_SERVO -> Xp5SettingServoFragment()
            Xp5SettingsViewModel.IDX_SWOFF -> Xp5SettingSwoffFragment()
            Xp5SettingsViewModel.IDX_COUPLING -> Xp5SettingCoplingFragment()
            else -> WipFragment()
        }
    }
}