/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

// https://stackoverflow.com/a/65544704
class LockableScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {
    private var _enabled = true

    fun setScrollingEnabled(value: Boolean = true) {
        _enabled = value
    }

    fun isScrollable() = _enabled

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return when (ev?.action) {
            MotionEvent.ACTION_DOWN -> _enabled && super.onTouchEvent(ev)
            else -> super.onTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return _enabled && super.onInterceptTouchEvent(ev)
    }
}