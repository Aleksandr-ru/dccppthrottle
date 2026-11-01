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

        val rvAdapter = LocoRecyclerViewAdapter(parentFragmentManager)
        val updateList = object : Runnable {
            override fun run() {
                if (_binding?.listLocos !== view) return
                if (view.isComputingLayout) {
                    view.post(this)
                    return
                }
                val locos = LocomotivesStore.data.value.orEmpty()
                rvAdapter.replaceValues(locos)
                rvAdapter.notifyDataSetChanged()
                view.visibility = if (locos.isEmpty()) View.GONE else View.VISIBLE
                placeholder.visibility = if (locos.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        LocomotivesStore.data.observe(viewLifecycleOwner) {
            view.removeCallbacks(updateList)
            updateList.run()
        }

        with(view) {
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
