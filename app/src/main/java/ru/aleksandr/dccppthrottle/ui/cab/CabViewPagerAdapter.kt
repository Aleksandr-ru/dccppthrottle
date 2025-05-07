/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.cab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CabViewPagerAdapter(
    fragment: FragmentActivity,
    private val layoutId: Int,
    private val values: List<Int>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = values.size

    override fun createFragment(position: Int): Fragment {
        return CabFragment.newInstance(layoutId, values[position])
    }
}