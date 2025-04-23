/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

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
import ru.aleksandr.dccppthrottle.DecoderActivity
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore


class Xp4OutputsFragment : DecoderFragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Xp4OutputsViewModel>()

    private lateinit var emptyView: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var writeButton: Button

    companion object {
        fun newInstance() = Xp4OutputsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_xp4_outputs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            emptyView = findViewById(R.id.empty_view)
            viewPager = findViewById(R.id.pager)
            tabLayout = findViewById(R.id.tabLayout)
            writeButton = findViewById(R.id.buttonWrite)
        }

        val adapter = SlidePagerAdapter(this)
        viewPager.adapter = adapter

        val tabTitles = resources.getStringArray(R.array.xp4_output_tabs)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles.get(position)
        }.attach()

        emptyView.setOnClickListener {
            readAllCVs()
        }

        writeButton.setOnClickListener {
            val job = writeAllCVs()
            job.invokeOnCompletion {
                if (!job.isCancelled) activity?.onBackPressed()
            }
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
            Snackbar.make(view, R.string.message_cvs_outdated, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_reload) {
                    readAllCVs()
                }.show()
        }
    }

    private fun readAllCVs() {
        val maxCvs = Xp4OutputsViewModel.cvNumbers.size
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
                val cv8 = readCv(DecoderActivity.MANUFACTURER_CV, MockStore::randomXp4OutputCvValue)
                if (cv8 != DecoderActivity.MANUFACTURER_ID_PIKO && cv8 != DecoderActivity.MANUFACTURER_ID_UHLENBROCK) {
                    val message = getString(R.string.message_wrong_manufacturer, cv8)
                    if (BuildConfig.DEBUG) Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    else throw Exception(message)
                }

                for (cv in Xp4OutputsViewModel.cvNumbers) {
                    val value = readCv(cv, MockStore::randomXp4OutputCvValue)
                    model.setCvValue(cv, value)
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

    private fun writeAllCVs(): Job {
        var job: Job? = null
        val dialog = ProgressDialog(context!!).apply {
            setTitle(R.string.title_dialog_writing_cvs)
            setMax(Xp4OutputsViewModel.cvNumbers.size)
            setNegativeButton(android.R.string.cancel) { _, _ -> job?.cancel() }
//            setOnDismissListener { model.editRow(null) }
            show()
        }

        job = lifecycleScope.launch {
            try {
                for (cv in Xp4OutputsViewModel.cvNumbers) {
                    val value = model.getCvValue(cv)
                    writeCv(cv, value)
                    dialog.incrementProgress()
                }
            }
            catch (e: Exception) {
                if (e !is CancellationException) {
                    if (BuildConfig.DEBUG) Log.w(TAG, e)
                    Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                }
                job?.cancel()
//                model.setLoaded(false)
            }
            dialog.dismiss()
        }

        return job
    }

    private inner class SlidePagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = resources.getStringArray(R.array.xp4_output_tabs).size

        override fun createFragment(position: Int) = when(position) {
            Xp4OutputsViewModel.IDX_DIMMING -> Xp4OutputDimmingFragment()
            Xp4OutputsViewModel.IDX_FADING -> Xp4OutputFadingFragment()
            Xp4OutputsViewModel.IDX_BLINKING -> Xp4OutputBlinkingFragment()
            Xp4OutputsViewModel.IDX_NEON -> Xp4OutputNeonFragment()
            Xp4OutputsViewModel.IDX_ESAVING -> Xp4OutputEsavingFragment()
            Xp4OutputsViewModel.IDX_FIREBOX -> Xp4OutputFireboxFragment()
            Xp4OutputsViewModel.IDX_SMOKE -> Xp4OutputSmokeFragment()
            else -> WipFragment()
        }
    }
}