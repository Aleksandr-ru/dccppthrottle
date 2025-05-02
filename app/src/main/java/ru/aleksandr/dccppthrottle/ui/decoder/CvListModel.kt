/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import ru.aleksandr.dccppthrottle.BuildConfig

open class CvListModel(vararg cvs: Int): ViewModel() {

    private val TAG = javaClass.simpleName
    private val key = javaClass.simpleName

    val cvNumbers = cvs.toList()

    init {
        if (!store.containsKey(key)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Creating store for '$key'")
            store[key] = State(cvNumbers)
        }
    }

    private val state = store[key]!!

    private var _loaded = MutableLiveData(state.cvValues.value?.any { it.value > 0 } ?: false)
    val loaded: LiveData<Boolean> = _loaded

    val hasChanges: LiveData<Boolean> = state.cvChanges.map { it.isNotEmpty() }.distinctUntilChanged()

    fun getCvValue(cv: Int): Int = state.cvChanges.value!!.getOrElse(cv) {
        state.cvValues.value!![cv]!!
    }

    fun getChanges() = state.cvChanges.value!!.toMap()

    fun setCvValue(cv: Int, value: Int, commit: Boolean = false) {
        if (commit) {
            state.cvChanges.postValue(state.cvChanges.value!!.also {
                it.remove(cv)
            })
            state.cvValues.postValue(state.cvValues.value!!.also {
                it[cv] = value
            })
        }
        else if (
            (!state.cvChanges.value!!.containsKey(cv) && state.cvValues.value!![cv] != value)
            || (state.cvChanges.value!!.containsKey(cv) && state.cvChanges.value!![cv] != value)
        ) {
            state.cvChanges.postValue(state.cvChanges.value!!.also {
                it[cv] = value
            })
        }
    }

    fun discardChanges() {
        state.cvChanges.postValue(state.cvChanges.value!!.also {
            it.clear()
        })
    }

    fun commitChanges() {
        state.cvValues.postValue(state.cvValues.value!!.also {
            it.putAll(state.cvChanges.value!!)
        })
        discardChanges()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun liveCvValue(cv: Int): LiveData<Int> =
        merge(
            state.cvValues.map { it.getValue(cv) }.asFlow(),
            state.cvChanges.asFlow().filter { it.containsKey(cv) }.map { it.getValue(cv) }
        ).asLiveData()

    fun setLoaded(value: Boolean) {
        if (!value) {
            discardChanges()
            state.cvValues.postValue(state.cvValues.value!!.also { old ->
                old.map { it.key to 0 }
            })
        }
        _loaded.postValue(value)
    }

    private data class State(val cvNumbers: List<Int>){
        val cvValues: MutableLiveData<MutableMap<Int, Int>> =
            MutableLiveData(cvNumbers.associateWith { 0 }.toMutableMap())
        val cvChanges: MutableLiveData<MutableMap<Int, Int>> =
            MutableLiveData(emptyMap<Int, Int>().toMutableMap())
    }

    companion object {
        private val store = mutableMapOf<String, State>()
    }
}