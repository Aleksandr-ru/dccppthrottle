/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle

object Utility {
    // https://stackoverflow.com/a/929107
    fun remap(value: Float, oldMin: Float, oldMax: Float, newMin: Float, newMax: Float) : Float {
        val oldRange = (oldMax - oldMin)
        val newRange = (newMax - newMin)
        return (((value - oldMin) * newRange) / oldRange) + newMin
    }
}