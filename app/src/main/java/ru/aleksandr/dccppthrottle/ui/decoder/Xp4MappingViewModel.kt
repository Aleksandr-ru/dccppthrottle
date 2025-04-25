/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.Exception

class Xp4MappingViewModel : ViewModel() {

    private var _loaded = MutableLiveData(__loaded)
    val loaded: LiveData<Boolean> = _loaded

    private var _reloadRowIndex = MutableLiveData<Int?>(null)
    var reloadRowIndex: LiveData<Int?> = _reloadRowIndex

    private var _editRowIndex = MutableLiveData<Int?>(null)
    var editRowIndex: LiveData<Int?> = _editRowIndex

    private var _editRowValues = Array(COLS) { 0 }

    private val START_CV = 257
    private val START_IDX = 0
    private val ROWS_IN_IDX = 16

    val inputColumnIndexes get() = (0 until INPUTS)
    val outputColumnIndexes get() = (INPUTS until COLS)
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

    fun cvNumber(rowIndex: Int, colIndex: Int): Pair<Int, Int> {
        if (rowIndex > ROWS) throw Exception("Mapping row $rowIndex out of bounds")
        if (colIndex > COLS) throw Exception("Mapping column $colIndex out of bounds")
        val num = START_CV + (rowIndex % COLS) * COLS + colIndex
        val idx = START_IDX + rowIndex / ROWS_IN_IDX
        return Pair(idx, num)
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

    fun isBlank(lineIndex: Int): Boolean = !cvValues[lineIndex].any {
        it != 0
    }

    companion object {
        private const val INPUTS = 12 // Bytes 1-12
        private const val OUTPUTS = 4 // Bytes 13-16
        const val COLS = INPUTS + OUTPUTS
        const val ROWS = 32

        const val INDEX_CV1 = 31
        const val INDEX_CV1_VALUE = 8
        const val INDEX_CV2 = 32
        const val EXTENDED_MAPPING_CV = 96
        const val EXTENDED_MAPPING_CV_VALUE = 1

        private var __loaded = false
        private val cvValues = List(ROWS) { Array(COLS) { 0 }}
    }
}