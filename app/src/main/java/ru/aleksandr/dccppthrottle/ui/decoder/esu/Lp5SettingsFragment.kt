/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.esu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore
import ru.aleksandr.dccppthrottle.ui.decoder.DecoderFragment
import ru.aleksandr.dccppthrottle.ui.decoder.WipFragment


class Lp5SettingsFragment : DecoderFragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Lp5SettingsViewModel>()

    private lateinit var emptyView: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var writeButton: Button

    companion object {
        fun newInstance() = Lp5SettingsFragment()
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

        val tabTitles = resources.getStringArray(R.array.lp5_conf_tabs)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles.get(position)
        }.attach()

        emptyView.setOnClickListener {
            readAllCVs()
        }

        writeButton.setOnClickListener {
            val job = writeChangedCVs()
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
                    readAllCVs()
                }.show()
        }
    }

    private fun readAllCVs() {
        val maxCvs = model.cvNumbers.size
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setMax(maxCvs)
            setTitle(getString(R.string.title_dialog_reading_x_cvs, maxCvs))
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            show()
        }

        model.setLoaded(false)

        job = lifecycleScope.launch {
            try {
                checkManufacturer(MANUFACTURER_ID_PIKO, MANUFACTURER_ID_UHLENBROCK)

                for (cv in model.cvNumbers) {
                    val value = readCv(cv, MockStore::randomDecoderSettingCvValue)
                    model.setCvValue(cv, value, true)
                    dialog.incrementProgress()
                }
                model.setLoaded(true)
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    if (BuildConfig.DEBUG) Log.w(TAG, e)
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
                if (BuildConfig.DEBUG) {
                    model.setLoaded(true)
                }
            }
            dialog.dismiss()
        }
    }

    private fun writeChangedCVs(): Job {
        val changes = model.getChanges()
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setTitle(R.string.title_dialog_writing_cvs)
            setMax(model.cvNumbers.size)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
            show()
        }

        job = lifecycleScope.launch {
            try {
                changes.forEach {
                    writeCv(it.key, it.value)
                    dialog.incrementProgress()
                }
                model.commitChanges()
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    if (BuildConfig.DEBUG) Log.w(TAG, e)
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
                model.setLoaded(false)
            }
            dialog.dismiss()
        }

        return job
    }

    private inner class SlidePagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = resources.getStringArray(R.array.lp5_conf_tabs).size

        override fun createFragment(position: Int) = when(position) {
            Lp5SettingsViewModel.IDX_CONF -> Lp5SettingConfFragment()
            Lp5SettingsViewModel.IDX_COUPLERS -> Lp5SettingCouplersFragment()
            else -> WipFragment()
        }
    }
}