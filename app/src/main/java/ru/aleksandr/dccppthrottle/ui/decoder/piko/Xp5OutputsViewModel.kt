/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.piko

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleksandr.dccppthrottle.R
import java.lang.Exception

class Xp5OutputsViewModel : ViewModel() {

    private var _loaded = MutableLiveData(__loaded)
    val loaded: LiveData<Boolean> = _loaded

    private var _reloadRowIndex = MutableLiveData<Int?>(null)
    var reloadRowIndex: LiveData<Int?> = _reloadRowIndex

    private var _editRowIndex = MutableLiveData<Int?>(null)
    var editRowIndex: LiveData<Int?> = _editRowIndex

    private var _editRowValues = Array(COLS) { 0 }

    private val START_CV = 257

    val rowIndexes get() = (0 until ROWS)
    val colIndexes get() = (0 until COLS)

    fun editRow(rowIndex: Int?) {
        if (rowIndex != null) {
            _editRowValues = cvValues[rowIndex].clone()
        }
        _editRowIndex.postValue(rowIndex)
    }

    fun reloadRow(rowIndex: Int?) {
        _reloadRowIndex.postValue(rowIndex)
    }

    fun getEditRowValue(colIndex: Int) = _editRowValues[colIndex]

    fun setEditRowValue(colIndex: Int, value: Int) {
        _editRowValues[colIndex] = value
    }

    fun cvNumber(rowIndex: Int, colIndex: Int): Int {
        if (rowIndex > ROWS) throw Exception("Row $rowIndex out of bounds")
        if (colIndex > COLS) throw Exception("Column $colIndex out of bounds")
        return START_CV + rowIndex * COLS + colIndex
    }

    fun getCvValue(rowIndex: Int, colIndex: Int): Int {
        return cvValues[rowIndex][colIndex]
    }

    fun setCvValue(rowIndex: Int, colIndex: Int, value: Int) {
        cvValues[rowIndex][colIndex] = value
    }

    fun setLoaded(value: Boolean) {
        if (!value) cvValues.forEach {
            it.fill(0)
        }
        __loaded = value
        _loaded.postValue(__loaded)
    }

    fun getEffectsMap(context: Context): Map<Int, String> {
        val stringArray = context.resources.getStringArray(R.array.xp5_output_effects)
        val regex = Regex("""^(\d+): (.+)$""")
        return stringArray.mapNotNull { str ->
            regex.matchEntire(str)?.let {
                it.groupValues[1].toInt() to it.groupValues[2]
            }
        }.toMap()
    }

    companion object {
        const val COLS = 10
        const val ROWS = 9

        const val COL_EFFECTA = 0
        const val COL_PWMA = 1
        const val COL_FLAGSA = 2
        const val COL_PARAM1A = 3
        const val COL_PARAM2A = 4
        const val COL_EFFECTB = 5
        const val COL_PWMB = 6
        const val COL_FLAGSB = 7
        const val COL_PARAM1B = 8
        const val COL_PARAM2B = 9

        val INDEX_CV1 = Pair(31, 18)
        val INDEX_CV2 = Pair(32, 0)

        private var __loaded = false
        private val cvValues = List(ROWS) { Array(COLS) { 0 }}
    }
}