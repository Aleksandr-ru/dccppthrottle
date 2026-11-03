package ru.aleksandr.dccppthrottle

import android.util.Log
import android.content.Intent
import android.widget.ImageButton
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2OutputsRecyclerViewAdapter
import ru.aleksandr.dccppthrottle.ui.decoder.modelldepo.Sw2OutputsViewModel

@RunWith(AndroidJUnit4::class)
class Sw2OutputsRecyclingTest {
    @Test
    fun scrollingPreservesAllCvsAndEditsOnlyTheVisibleChannel() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        lateinit var model: Sw2OutputsViewModel
        var savedValues = emptyMap<Int, Int>()
        var savedChanges = emptyMap<Int, Int>()
        var initialValues = emptyMap<Int, Int>()
        instrumentation.runOnMainSync {
            model = Sw2OutputsViewModel()
            savedValues = model.cvNumbers.associateWith { model.getCvValue(it) }
            savedChanges = model.getChanges()
            model.discardChanges()
            for (channel in 0..12) {
                model.setCvValue(30 + channel, listOf(0, 1, 5, 55)[channel % 4], true)
                model.setCvValue(46 + channel, 30 + channel * 4, true)
                model.setCvValue(62 + channel, 3 + channel, true)
                model.setCvValue(78 + channel, 6 + channel, true)
            }
            initialValues = model.cvNumbers.associateWith { model.getCvValue(it) }
        }
        instrumentation.waitForIdleSync()
        try {
            val intent = Intent(context, DecoderActivity::class.java)
                .putExtra("menuid", R.id.action_sw2_outputs)
            ActivityScenario.launch<DecoderActivity>(intent).use { scenario ->
                scenario.onActivity {
                    ViewModelProvider(it)[Sw2OutputsViewModel::class.java].setLoaded(true)
                }
                instrumentation.waitForIdleSync()
                // Traverse the real RecyclerView in both directions to force row reuse.
                for (position in (0..12).toList() + (12 downTo 0).toList()) {
                    scenario.onActivity {
                        it.findViewById<RecyclerView>(R.id.rvOutputs).scrollToPosition(position)
                    }
                    instrumentation.waitForIdleSync()
                    scenario.onActivity {
                        assertTrue("Scrolling to $position created CV edits: ${model.getChanges()}",
                            model.getChanges().isEmpty())
                        assertEquals(initialValues, model.cvNumbers.associateWith { model.getCvValue(it) })
                    }
                }
                scenario.onActivity {
                    val list = it.findViewById<RecyclerView>(R.id.rvOutputs)
                    val holder = list.findViewHolderForAdapterPosition(0) as Sw2OutputsRecyclerViewAdapter.ViewHolder
                    assertEquals(30, holder.dayBrightnessView.value)
                    assertEquals(3, holder.nightBrightnessView.value)
                    assertEquals(6, holder.fadeSpeedView.value)
                    for (input in listOf(holder.dayBrightnessView, holder.nightBrightnessView, holder.fadeSpeedView)) {
                        input.findViewById<ImageButton>(R.id.buttonPlus).performClick()
                    }
                    holder.effectView.setSelection(1)
                }
                instrumentation.waitForIdleSync()
                scenario.onActivity {
                    assertEquals("All four edits must belong to HL1 only",
                        mapOf(30 to 1, 46 to 31, 62 to 4, 78 to 7), model.getChanges())
                }
            }
        } finally {
            instrumentation.runOnMainSync {
                model.discardChanges()
                savedValues.forEach { (cv, value) -> model.setCvValue(cv, value, true) }
            }
            instrumentation.waitForIdleSync()
            instrumentation.runOnMainSync {
                savedChanges.forEach { (cv, value) -> model.setCvValue(cv, value) }
            }
        }
    }

    @Test
    fun recycledRowMustNotCopyPreviousChannelsBrightnessIntoCurrentChannel() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var model: Sw2OutputsViewModel
        lateinit var adapter: Sw2OutputsRecyclerViewAdapter
        lateinit var holder: Sw2OutputsRecyclerViewAdapter.ViewHolder
        var savedValues = emptyMap<Int, Int>()
        var savedChanges = emptyMap<Int, Int>()
        instrumentation.runOnMainSync {
            model = Sw2OutputsViewModel()
            savedValues = model.cvNumbers.associateWith { model.getCvValue(it) }
            savedChanges = model.getChanges()
            model.discardChanges()
            // Valid values for every channel. Only day brightness differs.
            for (channel in 0..12) {
                model.setCvValue(30 + channel, 5, true)
                model.setCvValue(46 + channel, 30 + channel * 4, true)
                model.setCvValue(62 + channel, 3, true)
                model.setCvValue(78 + channel, 6, true)
            }
        }
        instrumentation.waitForIdleSync()
        try {
            ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    adapter = Sw2OutputsRecyclerViewAdapter(model, activity)
                    holder = adapter.onCreateViewHolder(FrameLayout(activity), 0)
                    activity.setContentView(FrameLayout(activity).apply { addView(holder.itemView) })
                    adapter.onBindViewHolder(holder, 0)
                }
                instrumentation.waitForIdleSync()
                scenario.onActivity {
                    assertEquals(30, holder.dayBrightnessView.value)
                    // The same callbacks RecyclerView uses when reusing a row after scrolling.
                    adapter.onViewRecycled(holder)
                    adapter.onBindViewHolder(holder, 1)
                }
                instrumentation.waitForIdleSync()
                scenario.onActivity {
                    assertEquals(34, holder.dayBrightnessView.value)
                    assertEquals(34, model.getCvValue(47))
                    model.discardChanges()
                }
                instrumentation.waitForIdleSync()
                scenario.onActivity {
                    // Update HL1 while the reused row now displays HL2.
                    model.setCvValue(46, 25)
                }
                instrumentation.waitForIdleSync()
                scenario.onActivity {
                    Log.i("Sw2OutputsRecyclingTest", "After editing CV46=25: changes=${model.getChanges()}, " +
                        "HL2 CV47=${model.getCvValue(47)}, displayed=${holder.dayBrightnessView.value}")
                    assertEquals("Editing HL1 must change only CV46, not HL2's CV47",
                        mapOf(46 to 25), model.getChanges())
                    assertEquals("HL2 brightness must remain unchanged", 34, holder.dayBrightnessView.value)
                }
            }
        } finally {
            instrumentation.runOnMainSync {
                model.discardChanges()
                savedValues.forEach { (cv, value) -> model.setCvValue(cv, value, true) }
            }
            instrumentation.waitForIdleSync()
            instrumentation.runOnMainSync {
                savedChanges.forEach { (cv, value) -> model.setCvValue(cv, value) }
            }
        }
    }
}
