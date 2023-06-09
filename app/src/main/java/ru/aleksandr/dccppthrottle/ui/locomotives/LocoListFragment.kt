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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.dialogs.LocomotiveDialog
import ru.aleksandr.dccppthrottle.databinding.FragmentLocoListBinding as FragmentLocomotivesBinding
import ru.aleksandr.dccppthrottle.store.LocomotivesStore
import ru.aleksandr.dccppthrottle.store.MockStore

class LocoListFragment : Fragment() {

    private var _binding: FragmentLocomotivesBinding? = null

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

        if (view is RecyclerView) {
            val rvAdapter = LocoRecyclerViewAdapter(parentFragmentManager)
            LocomotivesStore.data.observe(viewLifecycleOwner) {
                rvAdapter.replaceValues(it)
                if (!view.isComputingLayout) {
                    rvAdapter.notifyDataSetChanged()
                }

                if (it.isEmpty()) {
                    view.visibility = View.GONE
                    placeholder.visibility = View.VISIBLE
                }
                else {
                    view.visibility = View.VISIBLE
                    placeholder.visibility = View.GONE
                }
            }

            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = rvAdapter
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}