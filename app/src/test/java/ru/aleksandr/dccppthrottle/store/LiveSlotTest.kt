package ru.aleksandr.dccppthrottle.store

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

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

}
