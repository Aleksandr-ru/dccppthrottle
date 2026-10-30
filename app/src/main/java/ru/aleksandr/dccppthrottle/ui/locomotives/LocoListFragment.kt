/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.locomotives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.map
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.databinding.FragmentLocoListBinding as FragmentLocomotivesBinding
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.MockStore

class LocoListFragment : Fragment() {

    private var _binding: FragmentLocomotivesBinding? = null
    private var submitLatest: Runnable? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocomotivesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val view = binding.listLocos
        val placeholder = binding.emptyView

        placeholder.setOnClickListener {
            if (BuildConfig.DEBUG) for (i in 1..10)
                LocomotivesStore.add(MockStore.randomLocomotive(false))
            else {
                LocomotiveDialog.storeIndex = -1
                LocomotiveDialog().show(parentFragmentManager, LocomotiveDialog.TAG)
            }
        }

        val rvAdapter = LocoRecyclerViewAdapter(parentFragmentManager)
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = rvAdapter
        (view.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.supportsChangeAnimations = false

        var pendingRows: List<LocoListItem>? = null
        val submit = Runnable {
            val rows = pendingRows
            pendingRows = null
            if (_binding?.listLocos === view && rows != null) {
                rvAdapter.submitList(rows)
                view.visibility = if (rows.isEmpty()) View.GONE else View.VISIBLE
                placeholder.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        submitLatest = submit
        LocomotivesStore.data.map { LocoListItem.snapshot(it) }.distinctUntilChanged()
            .observe(viewLifecycleOwner) { rows ->
                pendingRows = rows
                // First insert and clear can dispatch synchronously in ListAdapter.
                // Always submit outside RecyclerView's layout; coalesce to the latest snapshot.
                view.removeCallbacks(submit)
                view.post(submit)
            }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        submitLatest?.let { binding.listLocos.removeCallbacks(it) }
        submitLatest = null
        _binding = null
    }
}