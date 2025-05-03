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


class Xp5SimpleMappingFragment : DecoderFragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Xp5SimpleMappingViewModel>()

    private lateinit var emptyView: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var writeButton: Button

    companion object {
        fun newInstance() = Xp5SimpleMappingFragment()
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

        val tabTitles = resources.getStringArray(R.array.xp5_simple_keys).toMutableList().also {
            it.add(0, getString(R.string.label_setup))
        }.toList()
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
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
        override fun getItemCount(): Int = resources.getStringArray(R.array.xp5_simple_keys).size + 1

        override fun createFragment(position: Int) = when(position) {
            0 -> Xp5SimpleSetupFragment()
            else -> Xp5SimpleKeyFragment.newInstance(position -1)
        }
    }
}