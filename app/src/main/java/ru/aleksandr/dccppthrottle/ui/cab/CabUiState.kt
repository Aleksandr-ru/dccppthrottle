package ru.aleksandr.dccppthrottle.ui.cab

import ru.aleksandr.dccppthrottle.Utility.remap
import ru.aleksandr.dccppthrottle.store.LocomotivesStore.LocomotiveState
import kotlin.math.roundToInt

internal data class CabMotion(val speed: Int, val reverse: Boolean, val minSpeed: Int, val maxSpeed: Int)

/** Value snapshots allow comparison even though the store mutates objects in place. */
internal data class CabUiState(
    val id: Long,
    val title: String,
    val address: Int,
    val motion: CabMotion,
    val functionNames: List<String>,
    val functions: List<Boolean>
) {
    fun changedFunctions(previous: CabUiState?): List<Int> = functions.indices.filter {
        previous == null || id != previous.id || functions[it] != previous.functions[it]
    }

    companion object {
        fun forSlot(locos: List<LocomotiveState>, slot: Int): CabUiState? =
            locos.firstOrNull { it.slot == slot }?.let {
                CabUiState(
                    it.id, it.toString(), it.address,
                    CabMotion(it.speed, it.reverse, it.minSpeed, it.maxSpeed),
                    it.funcNames.toList(), it.functions.toList()
                )
            }
    }
}

internal data class FunctionLayout(val names: List<String>, val perRow: Int, val namedOnly: Boolean) {
    fun rows(): List<List<Int>> {
        val named = names.indices.filter { names[it].isNotEmpty() }
        val unnamed = names.indices.filter {
            names[it].isEmpty() && (!namedOnly || named.size <= 1 && it <= 12)
        }
        return named.map { listOf(it) } + unnamed.chunked(perRow.coerceAtLeast(1))
    }
}

internal object SpeedScale {
    fun toProgress(speed: Int, min: Int, max: Int): Int = when {
        speed <= 0 -> 0
        max <= min -> 100
        else -> remap(speed.toFloat(), min.toFloat(), max.toFloat(), 1F, 100F)
            .roundToInt().coerceIn(1, 100)
    }

    fun toSpeed(progress: Int, min: Int, max: Int): Int =
        if (progress <= 0) 0
        else remap(progress.coerceIn(1, 100).toFloat(), 1F, 100F, min.toFloat(), max.toFloat()).roundToInt()
}
