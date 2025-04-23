/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.view

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import ru.aleksandr.dccppthrottle.R


class PlusMinusView : LinearLayout {

    private val TAG = javaClass.simpleName

    private var numberInput : EditText? = null
    private var _value: Int? = null

    private lateinit var plusButton: ImageButton
    private lateinit var minusButton: ImageButton

    var min: Int? = null
    var max: Int? = null
    var step: Int = 1
    var nullable: Boolean = false

    var value: Int?
        get() = _value
        set(value) {
            _value = if (value == null && !nullable) min ?: 0
            else if (value != null) {
                if (max != null && value > max!!) max
                else if (min != null && value < min!!) min
                else value
            } else null

            numberInput?.let {
                if (it.text.ifEmpty { null }.toString() != _value.toString()) {
                    it.setText(_value.toString())
                    it.setSelection(it.text.length)
                }
            }
        }

    private var onChangeListener : ((Int?) -> Unit)? = null

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
        LayoutInflater.from(context).inflate(R.layout.view_plus_minus, this, true)
        numberInput = findViewById<EditText>(R.id.editTextNumber)

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.PlusMinusView, defStyle, 0
        )

        if (a.hasValue(R.styleable.PlusMinusView_min)) {
            min = a.getInt(R.styleable.PlusMinusView_min, 1)
        }
        if (a.hasValue(R.styleable.PlusMinusView_max)) {
            max = a.getInt(R.styleable.PlusMinusView_max, 100)
        }
        if (a.hasValue(R.styleable.PlusMinusView_step)) {
            step = a.getInt(R.styleable.PlusMinusView_step, 1)
        }
        if (a.hasValue(R.styleable.PlusMinusView_nullable)) {
            nullable = a.getBoolean(R.styleable.PlusMinusView_nullable, false)
        }
        if (a.hasValue(R.styleable.PlusMinusView_value)) {
            value = a.getInt(R.styleable.PlusMinusView_value, 0)
        }
        if (a.hasValue(R.styleable.PlusMinusView_android_enabled)) {
            isEnabled = a.getBoolean(R.styleable.PlusMinusView_android_enabled, true)
        }

        a.recycle()

        plusButton = findViewById(R.id.buttonPlus)
        minusButton = findViewById(R.id.buttonMinus)

        plusButton.setOnClickListener {
            value = numberInput?.text.toString().toIntOrNull()?.plus(step) ?: (max ?: 1)
            numberInput?.let {
                it.requestFocus()
                it.selectAll()
            }
        }

        minusButton.setOnClickListener {
            value = numberInput?.text.toString().toIntOrNull()?.minus(step) ?: (min ?: 0)
            numberInput?.let {
                it.requestFocus()
                it.selectAll()
            }
        }

        numberInput?.let {
            it.doAfterTextChanged { editable ->
                if (hasFocus()) {
                    value = editable.toString().toIntOrNull()
                }
                if (editable.toString().ifEmpty { null }.toString() != _value.toString()) {
                    it.setText(_value.toString().ifEmpty { null })
                    it.selectAll()
                }
                else if (onChangeListener != null) {
                    onChangeListener?.invoke(_value)
                }
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        children.forEach { it.isEnabled = enabled }

        // https://stackoverflow.com/a/69704169
        val plusDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_plus_24, null)
        val minusDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_minus_24, null)
        if (enabled) {
            plusButton.setImageDrawable(plusDrawable)
            minusButton.setImageDrawable(minusDrawable)
        }
        else {
            plusButton.setImageDrawable(convertToGrayscale(plusDrawable))
            minusButton.setImageDrawable(convertToGrayscale(minusDrawable))
        }
    }

    fun setOnChangeListener(listener: ((Int?) -> Unit)?) {
        onChangeListener = listener
    }

    fun change() {
        onChangeListener?.invoke(_value)
    }

    // https://stackoverflow.com/a/17112876
    private fun convertToGrayscale(drawable: Drawable?) = drawable?.mutate()?.apply {
        colorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        alpha = 160
    }
}