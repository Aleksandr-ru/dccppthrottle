/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.accessories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.databinding.FragmentAccessoriesBinding
import ru.aleksandr.dccppthrottle.dialogs.AccessoryDialog
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.MockStore

class AccessoriesFragment : Fragment() {

    @Suppress("unused")
    private val TAG = javaClass.simpleName

    private var _binding: FragmentAccessoriesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccessoriesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val view = binding.listAccessories
        val placeholder = binding.emptyView

        placeholder.setOnClickListener {
            if (BuildConfig.DEBUG) {
                for (i in 1..10) try {
                    AccessoriesStore.add(MockStore.randomAccessory())
                }
                catch (ex: Exception) {
                    Log.d(TAG, ex.toString())
                }
            }
            else {
                AccessoryDialog.storeIndex = -1
                AccessoryDialog().show(parentFragmentManager, AccessoryDialog.TAG)
            }
        }

        val rvAdapter = AccessoriesRecyclerViewAdapter(parentFragmentManager)
        val updateList = object : Runnable {
            override fun run() {
                if (_binding?.listAccessories !== view) return
                if (view.isComputingLayout) {
                    view.post(this)
                    return
                }
                val items = AccessoriesStore.data.value.orEmpty()
                rvAdapter.replaceValues(items)
                rvAdapter.notifyDataSetChanged()
                view.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                placeholder.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        AccessoriesStore.data.observe(viewLifecycleOwner) {
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