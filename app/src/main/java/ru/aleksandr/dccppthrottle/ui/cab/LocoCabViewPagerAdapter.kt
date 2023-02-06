package ru.aleksandr.dccppthrottle.ui.cab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.aleksandr.dccppthrottle.store.LocomotivesStore

class LocoCabViewPagerAdapter(
    fragment: FragmentActivity,
) : FragmentStateAdapter(fragment) {
    private var values: List<LocomotivesStore.LocomotiveSlot> = listOf()

    fun replaceValues(newValues: List<LocomotivesStore.LocomotiveSlot>) {
        values = newValues
        // notifyDataSetChanged()
        // Cannot call this method while RecyclerView is computing a layout or scrolling
    }

    override fun getItemCount(): Int = values.size

    override fun createFragment(position: Int): Fragment {
        return LocoCabFragment.newInstance(values[position].slot)
    }
}