package ru.aleksandr.dccppthrottle.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import ru.aleksandr.dccppthrottle.R


/**
 * TODO: document your custom view class.
 */
class PlusMinusView : LinearLayout {

    private var numberInput : EditText? = null
    private var _value: Int? = null

    var min: Int? = null
    var max: Int? = null
    var step: Int = 1
    var nullable: Boolean = false

    var value: Int?
        get() = _value
        set(value) {
            _value = if (value == null && !nullable) 0
            else if (value != null) {
                if (max != null && value > max!!) max
                else if (min != null && value < min!!) min
                else value
            } else null

            if (numberInput != null && numberInput!!.text.toString() != _value.toString()) {
                numberInput!!.setText(_value.toString())
            }
        }

    var onChangeListener : ((Int?) -> Unit)? = null

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
        LayoutInflater.from(context).inflate(R.layout.plus_minus_view, this, true)
        numberInput = findViewById<EditText>(R.id.editTextNumber)

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.PlusMinusView, defStyle, 0
        )

        if (a.hasValue(R.styleable.PlusMinusView_value)) {
            value = a.getInt(R.styleable.PlusMinusView_value, 0)
        }
        if (a.hasValue(R.styleable.PlusMinusView_min)) {
            min = a.getInt(R.styleable.PlusMinusView_min, 1)
        }
        if (a.hasValue(R.styleable.PlusMinusView_max)) {
            max = a.getInt(R.styleable.PlusMinusView_max, 100)
        }
        if (a.hasValue(R.styleable.PlusMinusView_step)) {
            step = a.getInt(R.styleable.PlusMinusView_step, 1)
        }

        a.recycle()

        val plusButton = findViewById<ImageButton>(R.id.buttonPlus)
        val minusButton = findViewById<ImageButton>(R.id.buttonMinus)

        plusButton.setOnClickListener {
            value = numberInput?.text.toString().toIntOrNull()?.plus(step) ?: (max ?: 1)
        }

        minusButton.setOnClickListener {
            value = numberInput?.text.toString().toIntOrNull()?.minus(step) ?: (min ?: 0)
        }

        numberInput!!.doAfterTextChanged {
            if (hasFocus()) {
                value = it.toString().toIntOrNull()
            }
            if (it.toString() != _value.toString()) {
                numberInput!!.setText(_value.toString())
            }
            else if (onChangeListener != null) {
                onChangeListener?.invoke(_value)
            }
        }
    }
}