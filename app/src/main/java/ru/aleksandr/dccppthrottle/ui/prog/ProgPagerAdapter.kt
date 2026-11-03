package ru.aleksandr.dccppthrottle.ui.prog

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProgPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ProgFragment()
        1 -> SpeedCurveFragment.newInstance(false)
        2 -> SpeedCurveFragment.newInstance(true)
        else -> error("Invalid programming tab $position")
    }
}
