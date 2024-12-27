/*
 * Copyright (c) 2024. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.aleksandr.dccppthrottle.R
import kotlin.math.pow

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

//    val rowsCount get() = ROWS
//    val columnsCount get() = COLS
//    val inputsBlockSize get() = CONDITIONS
//    val outputsBlockSize get() = OUTPUTS

//    val inputsIndexCvValues get() = (START_IDX .. (START_IDX + 4))
//    val outputsIndexCvValues get() = ((START_IDX + 5).. (START_IDX + 5 + 4))

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

    fun inputsString(context: Context, rowIndex: Int): String {
        val result = mutableListOf<String>()
        for (ci in inputColumnIndexes) {
            result += controlCvToStrings(context, ci, cvValues[rowIndex][ci])
        }
        return result.joinToString(", ")
    }

    fun outputsString(context: Context, rowIndex: Int): String {
        val result = mutableListOf<String>()
        for (ci in outputColumnIndexes) {
            result += controlCvToStrings(context, ci, cvValues[rowIndex][ci])
        }
        return result.joinToString(", ")
    }

    private fun controlCvToStrings(context: Context, colIndex: Int, value: Int): List<String> {
        val stringId = CONTROL_CV_STRING_ID[colIndex]
        val stringList = getStringList(context, stringId)
        return controlCvValueToStrings(value, stringList)
    }

    fun getStringList(context: Context, id: Int): List<String> {
        return context.resources.getStringArray(id).toList()
    }

    private fun controlCvValueToStrings(value: Int, strings: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (i in strings.indices) {
            val ii = 2f.pow(i).toInt()
            if (value and ii == ii) result.add(strings[i])
        }
        return result.toList()
    }

//    enum class ControlCv {
//        A, B, C, D, E, F, G, H, I, J,
//        K, L, M, N, O, P, Q
//    }

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