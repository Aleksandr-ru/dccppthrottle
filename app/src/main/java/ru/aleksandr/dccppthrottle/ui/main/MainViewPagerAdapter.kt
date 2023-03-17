/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.aleksandr.dccppthrottle.MainActivity
import ru.aleksandr.dccppthrottle.ui.accessories.AccessoriesFragment
import ru.aleksandr.dccppthrottle.ui.locomotives.LocoListFragment
import ru.aleksandr.dccppthrottle.ui.routes.RoutesFragment

class MainViewPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            MainActivity.POSITION_LOCOMOTIVES -> LocoListFragment()
            MainActivity.POSITION_ACCESSORIES -> AccessoriesFragment()
            MainActivity.POSITION_ROUTES -> RoutesFragment()
            else -> throw Throwable("Invalid position $position")
        }
    }
}