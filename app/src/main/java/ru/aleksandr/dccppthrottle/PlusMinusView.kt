package ru.aleksandr.dccppthrottle

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged


/**
 * TODO: document your custom view class.
 */
class PlusMinusView : LinearLayout {

    private var _value: Int? = null
    private var _min: Int? = null
    private var _max: Int? = null
    private var _nullable: Boolean = false
    private var _onChangeListener : ((Int?) -> Unit)? = null

    var value: Int?
        get() = _value
        set(value) {
            _value = value
        }

    var min: Int?
        get() = _min
        set(value) {
            _min = value
        }

    var max: Int?
        get() = _max
        set(value) {
            _max = value
        }

    var nullable: Boolean
        get() = _nullable
        set(value) {
            _nullable = value
        }

    var onChangeListener : ((Int?) -> Unit)?
        get() = _onChangeListener
        set(value) {
            _onChangeListener = value
        }

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

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.PlusMinusView, defStyle, 0
        )

        if (a.hasValue(R.styleable.PlusMinusView_value)) {
            _value = a.getInt(R.styleable.PlusMinusView_value, 0)
        }
        if (a.hasValue(R.styleable.PlusMinusView_min)) {
            _min = a.getInt(R.styleable.PlusMinusView_min, 1)
        }
        if (a.hasValue(R.styleable.PlusMinusView_max)) {
            _max = a.getInt(R.styleable.PlusMinusView_max, 100)
        }

        a.recycle()

        val plusButton = findViewById<Button>(R.id.buttonPlus)
        val minusButton = findViewById<Button>(R.id.buttonMinus)
        val numberInput = findViewById<EditText>(R.id.editTextNumber)

        plusButton.setOnClickListener {
            val n : Int = numberInput.text.toString().toIntOrNull()?.plus(1) ?: _max ?: 0
            numberInput.setText(n.toString())
        }

        minusButton.setOnClickListener {
            val n : Int = numberInput.text.toString().toIntOrNull()?.minus(1) ?: _min ?: 0
            numberInput.setText(n.toString())
        }

        numberInput.doAfterTextChanged {
            _value = it.toString().toIntOrNull()
            if (_value == null && !_nullable) _value = 0
            if (_value != null) {
                if (_max != null && _value!! > _max!!) {
                    _value = _max
                }
                else if (_min != null && _value!! < _min!!) {
                    _value = _min
                }
            }
            numberInput.setText(_value.toString())

            if (_onChangeListener != null) _onChangeListener?.invoke(_value)
        }
    }
}