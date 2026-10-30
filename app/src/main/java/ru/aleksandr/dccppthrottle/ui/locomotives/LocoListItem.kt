package ru.aleksandr.dccppthrottle.ui.locomotives

import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.LocomotivesStore.LocomotiveState
import ru.aleksandr.dccppthrottle.ui.cab.SpeedScale

/** Only fields displayed in the list, detached from the mutable store. */
internal data class LocoListItem(
    val id: Long,
    val title: String,
    val address: Int,
    val slot: Int,
    val assignmentEnabled: Boolean,
    val speed: Int,
    val reverse: Boolean,
    val progress: Int
) {
    fun changesFrom(old: LocoListItem): Int {
        var changes = 0
        if (title != old.title || address != old.address) changes = changes or DETAILS
        if (slot != old.slot || assignmentEnabled != old.assignmentEnabled) changes = changes or ASSIGNMENT
        if (speed != old.speed || reverse != old.reverse || progress != old.progress) changes = changes or MOVEMENT
        return changes
    }

    companion object {
        const val DETAILS = 1
        const val ASSIGNMENT = 2
        const val MOVEMENT = 4
        const val ALL = DETAILS or ASSIGNMENT or MOVEMENT

        fun snapshot(locos: List<LocomotiveState>): List<LocoListItem> {
            val hasFreeSlots = locos.count { it.slot > 0 } < LocomotivesStore.SLOTS_COUNT
            return locos.map {
                LocoListItem(
                    it.id, it.toString(), it.address, it.slot, it.slot > 0 || hasFreeSlots,
                    it.speed, it.speed > 0 && it.reverse,
                    SpeedScale.toProgress(it.speed, it.minSpeed, it.maxSpeed)
                )
            }
        }
    }
}
