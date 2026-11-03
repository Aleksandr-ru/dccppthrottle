package ru.aleksandr.dccppthrottle.ui.prog

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SpeedCurveViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val restored = savedState.get<Bundle>("curve")
    private val _values = MutableLiveData(defaults() +
        (restored?.keySet()?.associate { it.toInt() to restored.getInt(it) } ?: emptyMap()))
    val values: LiveData<Map<Int, Int>> = _values
    private val _simpleEnabled = MutableLiveData(savedState.get<Boolean>("simpleEnabled") ?: true)
    val simpleEnabled: LiveData<Boolean> = _simpleEnabled
    private val _smoothingEnabled = MutableLiveData(savedState.get<Boolean>("smoothingEnabled") ?: true)
    val smoothingEnabled: LiveData<Boolean> = _smoothingEnabled
    val busy = MutableLiveData(false)

    fun setSmoothingEnabled(enabled: Boolean) {
        if (_smoothingEnabled.value == enabled) return
        _smoothingEnabled.value = enabled
        savedState["smoothingEnabled"] = enabled
    }

    fun setValues(changes: Map<Int, Int>) {
        val updated = _values.value!! + changes.mapValues { it.value.coerceIn(0, 255) }
        if (updated == _values.value) return
        _values.value = updated
        savedState["curve"] = Bundle().apply { updated.forEach { (cv, value) -> putInt(cv.toString(), value) } }
    }

    fun setSimpleEnabled(enabled: Boolean) {
        if (_simpleEnabled.value == enabled) return
        _simpleEnabled.value = enabled
        savedState["simpleEnabled"] = enabled
    }

    fun applyRead(values: Map<Int, Int>) {
        setValues(values)
        setSimpleEnabled(simpleEnabled(values.getValue(29)))
    }

    /** NMRA CV layout and pure helpers shared by the two programming tabs. */
    companion object {
        private const val CONFIGURATION_CV = 29
        private const val SPEED_TABLE_BIT = 1 shl 4
        private val simplePoints = listOf(2, 6, 5)
        private val extendedPoints = (67..94).toList()

        internal fun pointCvs(extended: Boolean) = if (extended) extendedPoints else simplePoints
        internal fun dataCvs(extended: Boolean) = pointCvs(extended) + if (extended) listOf(66, 95) else emptyList()
        internal fun readCvs(extended: Boolean) = listOf(CONFIGURATION_CV) + dataCvs(extended)
        internal fun simpleEnabled(cv29: Int) = cv29 and SPEED_TABLE_BIT == 0
        internal fun withSimpleEnabled(cv29: Int, enabled: Boolean): Int =
            if (enabled) cv29 and SPEED_TABLE_BIT.inv() else cv29 or SPEED_TABLE_BIT

        private fun defaults(): Map<Int, Int> = mapOf(2 to 0, 6 to 128, 5 to 255, 66 to 0, 95 to 0) +
            extendedPoints.mapIndexed { index, cv -> cv to ((index + 1) * 255 / 28) }.toMap()
    }
}
