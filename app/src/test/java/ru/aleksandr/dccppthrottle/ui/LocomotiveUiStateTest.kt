package ru.aleksandr.dccppthrottle.ui

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import org.junit.Assert.*
import org.junit.Test
import ru.aleksandr.dccppthrottle.store.LocomotivesStore.LocomotiveState
import ru.aleksandr.dccppthrottle.ui.cab.CabUiState
import ru.aleksandr.dccppthrottle.ui.cab.FunctionLayout
import ru.aleksandr.dccppthrottle.ui.cab.SpeedScale
import ru.aleksandr.dccppthrottle.ui.locomotives.LocoListDiff
import ru.aleksandr.dccppthrottle.ui.locomotives.LocoListItem

class LocomotiveUiStateTest {
    private fun loco(address: Int = 3, slot: Int = 1) = LocomotiveState(address, "Train $address").apply {
        this.slot = slot
    }

    @Test
    fun functionUpdatesDoNotChangeListRows() {
        val loco = loco()
        val before = LocoListItem.snapshot(listOf(loco))
        loco.functions[5] = true
        loco.funcNames[5] = "Horn"
        loco.funcReset[5] = 1000
        assertEquals(before, LocoListItem.snapshot(listOf(loco)))
    }

    @Test
    fun speedUpdateHasOnlyMovementPayload() {
        val loco = loco()
        val before = LocoListItem.snapshot(listOf(loco)).single()
        loco.speed = 50
        loco.reverse = true
        val after = LocoListItem.snapshot(listOf(loco)).single()
        assertTrue(LocoListDiff.areItemsTheSame(before, after))
        assertFalse(LocoListDiff.areContentsTheSame(before, after))
        assertEquals(LocoListItem.MOVEMENT, LocoListDiff.getChangePayload(before, after))
        assertEquals(0, before.speed)
    }

    @Test
    fun editingTitleAndAddressPreservesIdentityAndChangesOnlyDetails() {
        val loco = loco()
        val before = LocoListItem.snapshot(listOf(loco)).single()
        loco.title = "New name"
        loco.address = 123
        val after = LocoListItem.snapshot(listOf(loco)).single()
        assertTrue(LocoListDiff.areItemsTheSame(before, after))
        assertEquals(LocoListItem.DETAILS, after.changesFrom(before))
        assertEquals("Train 3", before.title)
        assertEquals(3, before.address)
    }

    @Test
    fun duplicateAddressesStillHaveDifferentRowIds() {
        val rows = LocoListItem.snapshot(listOf(loco(), loco()))
        assertFalse(LocoListDiff.areItemsTheSame(rows[0], rows[1]))
    }

    @Test
    fun fillingLastSlotUpdatesAssignmentAvailabilityForOtherInactiveRows() {
        val locos = (1..11).map { loco(it, it) } + listOf(loco(12, 0), loco(13, 0))
        val before = LocoListItem.snapshot(locos)
        locos[11].slot = 12
        val full = LocoListItem.snapshot(locos)
        assertTrue(before[12].assignmentEnabled)
        assertFalse(full[12].assignmentEnabled)
        assertTrue(full[11].assignmentEnabled)
        assertEquals(LocoListItem.ASSIGNMENT, full[12].changesFrom(before[12]))
        assertEquals(before[0], full[0])
        locos[11].slot = 0
        assertTrue(LocoListItem.snapshot(locos)[12].assignmentEnabled)
    }

    @Test
    fun sortingProducesMovesWithoutChangingRowContents() {
        val locos = listOf(loco(2, 1), loco(1, 2))
        val before = LocoListItem.snapshot(locos)
        val after = LocoListItem.snapshot(locos.sortedBy { it.address })
        var moved = 0
        var changed = 0
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = before.size
            override fun getNewListSize() = after.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                LocoListDiff.areItemsTheSame(before[oldItemPosition], after[newItemPosition])
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                LocoListDiff.areContentsTheSame(before[oldItemPosition], after[newItemPosition])
        })
        diff.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) { fail("Unexpected insert") }
            override fun onRemoved(position: Int, count: Int) { fail("Unexpected remove") }
            override fun onMoved(fromPosition: Int, toPosition: Int) { moved++ }
            override fun onChanged(position: Int, count: Int, payload: Any?) { changed += count }
        })
        assertTrue(moved > 0)
        assertEquals(0, changed)
        assertEquals(before[0].id, after[1].id)
    }

    @Test
    fun updatesToAnotherLocomotiveDoNotChangeCabSnapshot() {
        val first = loco(3, 1)
        val second = loco(4, 2)
        val before = CabUiState.forSlot(listOf(first, second), 1)
        second.speed = 70
        second.functions[1] = true
        second.title = "Other train"
        assertEquals(before, CabUiState.forSlot(listOf(first, second), 1))
    }

    @Test
    fun cabFunctionDeltaContainsOnlyChangedButtonsAndKeepsOldSnapshot() {
        val loco = loco()
        val before = CabUiState.forSlot(listOf(loco), 1)!!
        loco.functions[5] = true
        loco.functions[28] = true
        val after = CabUiState.forSlot(listOf(loco), 1)!!
        assertEquals(listOf(5, 28), after.changedFunctions(before))
        assertEquals(before.motion, after.motion)
        assertEquals(before.functionNames, after.functionNames)
        assertFalse(before.functions[5])
        assertEquals(emptyList<Int>(), after.changedFunctions(after))
    }

    @Test
    fun cabSpeedChangesDoNotChangeFunctionLayoutOrButtons() {
        val loco = loco()
        val before = CabUiState.forSlot(listOf(loco), 1)!!
        loco.speed = 60
        val after = CabUiState.forSlot(listOf(loco), 1)!!
        assertNotEquals(before.motion, after.motion)
        assertEquals(before.functionNames, after.functionNames)
        assertTrue(after.changedFunctions(before).isEmpty())
    }

    @Test
    fun renamingFunctionChangesLayoutWithoutMutatingPreviousNames() {
        val loco = loco()
        val before = CabUiState.forSlot(listOf(loco), 1)!!
        loco.funcNames[2] = "Horn"
        val after = CabUiState.forSlot(listOf(loco), 1)!!
        assertEquals("", before.functionNames[2])
        assertEquals("Horn", after.functionNames[2])
        assertTrue(after.changedFunctions(before).isEmpty())
        assertNotEquals(FunctionLayout(before.functionNames, 4, false), FunctionLayout(after.functionNames, 4, false))
    }

    @Test
    fun changingResetTimerDoesNotRequireCabRendering() {
        val loco = loco()
        val before = CabUiState.forSlot(listOf(loco), 1)
        loco.funcReset[3] = 1500
        assertEquals(before, CabUiState.forSlot(listOf(loco), 1))
    }

    @Test
    fun missingSlotHasNoCabSnapshot() {
        assertNull(CabUiState.forSlot(listOf(loco()), 2))
    }

    @Test
    fun functionLayoutHandlesPartialRowsAndNamedButtons() {
        val names = List(29) { if (it == 5) "Horn" else "" }
        val rows = FunctionLayout(names, 4, false).rows()
        assertEquals(listOf(5), rows.first())
        assertEquals((0..28).toSet(), rows.flatten().toSet())
        assertEquals(29, rows.flatten().size)
        assertTrue(rows.drop(1).all { it.size <= 4 })
        val reduced = FunctionLayout(List(29) { "" }, 4, true).rows()
        assertEquals((0..12).toList(), reduced.flatten())
        assertEquals(1, reduced.last().size)
    }

    @Test
    fun namedOnlyWithMultipleNamedFunctionsHidesUnnamedButtons() {
        val names = List(29) { if (it == 1 || it == 28) "Named $it" else "" }
        assertEquals(listOf(listOf(1), listOf(28)), FunctionLayout(names, 2, true).rows())
    }

    @Test
    fun speedMappingPreservesStopAndConfiguredRange() {
        assertEquals(0, SpeedScale.toSpeed(0, 20, 80))
        assertEquals(20, SpeedScale.toSpeed(1, 20, 80))
        assertEquals(80, SpeedScale.toSpeed(100, 20, 80))
        assertEquals(0, SpeedScale.toProgress(0, 20, 80))
        assertEquals(1, SpeedScale.toProgress(20, 20, 80))
        assertEquals(100, SpeedScale.toProgress(80, 20, 80))
    }
}
