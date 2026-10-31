package ru.aleksandr.dccppthrottle.store

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.aleksandr.dccppthrottle.ui.cab.CabUiState

class LiveSlotTest {
    private val cleanup = mutableListOf<() -> Unit>()

    @Before
    fun setup() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread() = true
        })
        clearStore()
        LocomotivesStore.setSortOrder(LocomotivesStore.SORT_UNSORTED)
    }

    @After
    fun teardown() {
        cleanup.forEach { it() }
        clearStore()
        LocomotivesStore.hasUnsavedData = false
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    private fun clearStore() {
        while (LocomotivesStore.data.value!!.isNotEmpty()) LocomotivesStore.remove(0)
    }

    private fun loco(address: Int, slot: Int) = LocomotivesStore.LocomotiveState(address).apply {
        this.slot = slot
        LocomotivesStore.add(this)
    }

    private fun <T> record(source: LiveData<T>): MutableList<T?> {
        val values = mutableListOf<T?>()
        val observer = Observer<T> { values.add(it) }
        source.observeForever(observer)
        cleanup.add { source.removeObserver(observer) }
        return values
    }

    @Test
    fun missingAndRemovedSlotEmitNullWithoutThrowing() {
        val values = record(LocomotivesStore.liveSlot(1))
        assertEquals(listOf<LocomotivesStore.LocomotiveState?>(null), values)
        val loco = loco(3, 1)
        assertSame(loco, values.last())
        LocomotivesStore.remove(0)
        assertNull(values.last())
        assertEquals(3, values.size)
    }

    @Test
    fun otherLocomotiveChangesAreFilteredAfterCreatingSnapshot() {
        loco(3, 1)
        loco(4, 2)
        val snapshots = record(LocomotivesStore.liveSlot(1)
            .map { it?.let { loco -> CabUiState.from(loco) } }.distinctUntilChanged())
        LocomotivesStore.setSpeedBySlot(2, 70, true)
        LocomotivesStore.setFunctionBySlot(2, 5, true)
        assertEquals(1, snapshots.size)
    }

    @Test
    fun inPlaceMutationsProduceDistinctSnapshotsAndPreserveOldValues() {
        loco(3, 1)
        val snapshots = record(LocomotivesStore.liveSlot(1)
            .map { it?.let { loco -> CabUiState.from(loco) } }.distinctUntilChanged())
        LocomotivesStore.setSpeedBySlot(1, 40, false)
        LocomotivesStore.setFunctionBySlot(1, 5, true)
        LocomotivesStore.setFunctionBySlot(1, 5, true)
        assertEquals(3, snapshots.size)
        assertEquals(0, snapshots[0]!!.motion.speed)
        assertFalse(snapshots[1]!!.functions[5])
        assertEquals(40, snapshots[2]!!.motion.speed)
        assertTrue(snapshots[2]!!.functions[5])
    }
}
