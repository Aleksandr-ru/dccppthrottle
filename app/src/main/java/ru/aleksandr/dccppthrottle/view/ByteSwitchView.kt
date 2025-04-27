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
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.R

class ByteSwitchView : LinearLayout {

    private val TAG = javaClass.simpleName

    private var bits: List<SwitchCompat> = List(8) {
        SwitchCompat(context).apply {
            val m = resources.getDimension(R.dimen.text_margin)
            setPadding(m.toInt(), 0, m.toInt(), 0)
            switchPadding = m.toInt()
        }
    }

    private var changeListenerEnabled = true

    private var _strings = (0..8).map { String.format("Bit.%d", it) }.toTypedArray()

    private var _value: Int = 0
    private var _hidden: Int = 0

    var value: Int
        get() = _value
        set(value) {
            _value = value.toUByte().toInt()
            valueToBits()
        }

    var hiddenBits: Int
        get() = _hidden
        set(value) {
            _hidden = value.toUByte().toInt()
            hiddenToBits()
        }

    private var onChangeListener: ((Int) -> Unit)? = null
    private var onBitCheckedListener: ((Int, Boolean) -> Unit)? = null

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
        LayoutInflater.from(context).inflate(R.layout.view_byte_switch, this, true)

        orientation = VERTICAL

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ByteSwitchView, defStyle, 0
        )

        if (a.hasValue(R.styleable.ByteSwitchView_stringArray)) {
            a.getResourceId(R.styleable.ByteSwitchView_stringArray, 0).takeIf { it > 0 }?.also {
                _strings = resources.getStringArray(it)
            }
        }

        bits.withIndex().forEach { item ->
            addView(item.value)
            with(item.value) {
                _strings.getOrNull(item.index)?.let {
                    text = it
                }
                setOnCheckedChangeListener { _, checked ->
                    if (changeListenerEnabled) {
                        bitsToValue()
                        onChangeListener?.invoke(_value)
                        onBitCheckedListener?.invoke(item.index, checked)
                    }
                }
            }
        }

        if (a.hasValue(R.styleable.ByteSwitchView_value)) {
            value = a.getInt(R.styleable.ByteSwitchView_value, 0)
        }
        if (a.hasValue(R.styleable.ByteSwitchView_hiddenBits)) {
            hiddenBits = a.getInt(R.styleable.ByteSwitchView_hiddenBits, 0)
        }

        if (a.hasValue(R.styleable.ByteSwitchView_android_enabled)) {
            isEnabled = a.getBoolean(R.styleable.ByteSwitchView_android_enabled, true)
        }

        a.recycle()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        bits.forEach { it.isEnabled = enabled }
    }

    fun setOnChangeListener(listener: ((value: Int) -> Unit)?) {
        onChangeListener = listener
    }

    fun setOnBitCheckedListener(listener: ((index: Int, checked: Boolean) -> Unit)?) {
        onBitCheckedListener = listener
    }

    fun setBitText(index: Int, text: String) {
        bits.getOrNull(index)?.text = text
    }

    private fun valueToBits() {
        changeListenerEnabled = false
        bits.withIndex().forEach {
            val test = (1 shl it.index)
            it.value.isChecked = (_value.and(test) == test)
        }
        changeListenerEnabled = true
    }

    private fun bitsToValue() {
        var newValue = 0
        bits.withIndex().forEach {
            if (it.value.isChecked) newValue += (1 shl it.index)
        }
        _value = newValue

        if (BuildConfig.DEBUG)
            Log.d(TAG, String.format("bitsToValue = %d", _value))
    }

    private fun hiddenToBits() {
        bits.withIndex().forEach {
            it.value.visibility =
                if (_hidden.and(1 shl it.index) > 0) View.GONE
                else View.VISIBLE
        }
    }
}