package ru.aleksandr.dccppthrottle.ui.prog

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

import ru.aleksandr.dccppthrottle.store.SpeedCurveStore

class SpeedCurveViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val restored = savedState.get<Bundle>("curve")
    private val _values = MutableLiveData(SpeedCurveStore.defaults() +
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
        setSimpleEnabled(SpeedCurveStore.simpleEnabled(values.getValue(29)))
    }
}
