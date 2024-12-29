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
import ru.aleksandr.dccppthrottle.R
import java.lang.Exception

class Lp5MappingViewModel : ViewModel() {

    private var _loaded = MutableLiveData(__loaded)
    val loaded: LiveData<Boolean> = _loaded

    private var _editRowIndex = MutableLiveData<Int?>(null)
    var editRowIndex: LiveData<Int?> = _editRowIndex

    private var _editRowValues = Array(COLS) { 0 }

    private val START_CV = 257
    private val START_IDX = 3
    private val ROWS_IN_IDX = 16
    private val ROW_CV_BLOCK = 16

    val inputColumnIndexes get() = (0 until INPUTS)
    val outputColumnIndexes get() = (INPUTS until COLS)
    val rowIndexes get() = (0 until ROWS)

    fun editRow(rowIndex: Int?) {
        if (rowIndex != null) {
            _editRowValues = cvValues[rowIndex].clone()
        }
        _editRowIndex.postValue(rowIndex)
    }

    fun getEditRowValue(colIndex: Int) = _editRowValues[colIndex]

    fun setEditRowValue(colIndex: Int, value: Int) {
        _editRowValues[colIndex] = value
    }

    fun cvNumber(rowIndex: Int, colIndex: Int): Pair<Int, Int> {
        if (rowIndex > ROWS) throw Exception("Mapping row $rowIndex out of bounds")
        if (colIndex > COLS) throw Exception("Mapping column $colIndex out of bounds")
        val num = START_CV + ROW_CV_BLOCK * (rowIndex % ROWS_IN_IDX) + colIndex % INPUTS
        val idx = START_IDX + rowIndex / ROWS_IN_IDX + 5 * (colIndex / INPUTS)
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

    fun isBlank(rowIndex: Int): Boolean = !cvValues[rowIndex].any {
        it != 0
    }

    companion object {
        private const val INPUTS = 10 // Control CV A-J
        private const val OUTPUTS = 7 // Control CV K-Q
        const val COLS = INPUTS + OUTPUTS
        const val ROWS = 72

        const val INDEX_CV = 32

        val CONTROL_CV_LETTERS = ('A' .. 'Q').map { it }

        val CONTROL_CV_STRING_ID = listOf(
            R.array.lp5_control_cv_a,
            R.array.lp5_control_cv_b,
            R.array.lp5_control_cv_c,
            R.array.lp5_control_cv_d,
            R.array.lp5_control_cv_e,
            R.array.lp5_control_cv_f,
            R.array.lp5_control_cv_g,
            R.array.lp5_control_cv_h,
            R.array.lp5_control_cv_i,
            R.array.lp5_control_cv_j,
            R.array.lp5_control_cv_k,
            R.array.lp5_control_cv_l,
            R.array.lp5_control_cv_m,
            R.array.lp5_control_cv_n,
            R.array.lp5_control_cv_o,
            R.array.lp5_control_cv_p,
            R.array.lp5_control_cv_q,
        )

        private var __loaded = false
        private val cvValues = List(ROWS) { Array(COLS) { 0 }}
    }
}