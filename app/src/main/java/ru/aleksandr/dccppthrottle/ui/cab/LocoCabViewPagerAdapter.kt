package ru.aleksandr.dccppthrottle.ui.cab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.aleksandr.dccppthrottle.store.LocomotivesStore

class LocoCabViewPagerAdapter(
    fragment: FragmentActivity,
    private val values: List<LocomotivesStore.LocomotiveSlot>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return values.size
    }

    override fun createFragment(position: Int): Fragment {
        return LocoCabFragment.newInstance(values[position].slot)
    }
}