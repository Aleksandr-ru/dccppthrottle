/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R

class ByteChipsView : HorizontalScrollView {

    private val TAG = javaClass.simpleName

    private var chipsView: ChipGroup? = null
    private var changeListenerEnabled = true

    private var _strings = (0..8).map { it.toString() }.toTypedArray()

    private var _value: Int = 0
    private var _hidden: Int = 0

    var value: Int
        get() = _value
        set(value) {
            _value = value.toUByte().toInt()
            valueToChips()
        }

    var hiddenBits: Int
        get() = _hidden
        set(value) {
            _hidden = value.toUByte().toInt()
            hiddenToChips()
        }

    private var onChangeListener: ((Int) -> Unit)? = null
    private var onChipCheckedListener: ((Int, Boolean) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        LayoutInflater.from(context).inflate(R.layout.view_byte_chips, this, true)

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ByteChipsView, defStyle, 0
        )

        if (a.hasValue(R.styleable.ByteChipsView_stringArray)) {
            a.getResourceId(R.styleable.ByteChipsView_stringArray, 0).takeIf { it > 0 }?.also {
                _strings = resources.getStringArray(it)
            }
        }
        if (a.hasValue(R.styleable.ByteChipsView_value)) {
            value = a.getInt(R.styleable.ByteChipsView_value, 0)
        }
        if (a.hasValue(R.styleable.ByteChipsView_hiddenBits)) {
            hiddenBits = a.getInt(R.styleable.ByteChipsView_hiddenBits, 0)
        }

        chipsView = findViewById<ChipGroup>(R.id.chip_group).apply {
            children.withIndex().forEach { item ->
                val chip = item.value as Chip
                _strings.getOrNull(item.index)?.let {
                    chip.text = it
                }
                chip.setOnCheckedChangeListener { _, checked ->
                    if (changeListenerEnabled) {
                        chipsToValue()
                        onChipCheckedListener?.invoke(item.index, checked)
                        onChangeListener?.invoke(_value)
                    }
                }
            }
        }

        if (a.hasValue(R.styleable.ByteChipsView_android_enabled)) {
            isEnabled = a.getBoolean(R.styleable.ByteChipsView_android_enabled, true)
        }

        a.recycle()
        valueToChips()
        hiddenToChips()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        chipsView?.children?.forEach { it.isEnabled = enabled }
    }

    fun setOnChangeListener(listener: ((value: Int) -> Unit)?) {
        onChangeListener = listener
    }

    fun setOnChipCheckedListener(listener: ((index: Int, checked: Boolean) -> Unit)?) {
        onChipCheckedListener = listener
    }

    fun setChipText(index: Int, text: String) {
        val chip = chipsView?.children?.elementAtOrNull(index) as Chip?
        chip?.text = text
    }

    private fun valueToChips() {
        changeListenerEnabled = false
        chipsView?.children?.withIndex()?.forEach {
            val chip = it.value as Chip
            val test = (1 shl it.index)
            chip.isChecked = (_value.and(test) == test)
        }
        changeListenerEnabled = true
    }

    private fun chipsToValue() {
        var newValue = 0
        chipsView?.children?.withIndex()?.forEach {
            val chip = it.value as Chip
            if (chip.isChecked) newValue += (1 shl it.index)
        }
        _value = newValue

        if (BuildConfig.DEBUG)
            Log.d(TAG, String.format("chipsToValue = %d", _value))
    }

    private fun hiddenToChips() {
        chipsView?.children?.withIndex()?.forEach {
            it.value.visibility =
                if (_hidden.and(0b1 shl it.index) > 0) View.GONE
                else View.VISIBLE
        }
    }
}