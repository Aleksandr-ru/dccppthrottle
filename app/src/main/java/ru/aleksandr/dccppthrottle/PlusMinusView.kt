package ru.aleksandr.dccppthrottle

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout


/**
 * TODO: document your custom view class.
 */
class PlusMinusView : LinearLayout {

    private var _value: Int? = null
    private var _min: Int? = null
    private var _max: Int? = null
    private var _nullable: Boolean = false

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
    }
}