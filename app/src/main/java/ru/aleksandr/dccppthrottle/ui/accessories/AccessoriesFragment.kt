package ru.aleksandr.dccppthrottle.ui.accessories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.databinding.FragmentAccessoriesBinding
import ru.aleksandr.dccppthrottle.store.AccessoriesStore

class AccessoriesFragment : Fragment() {

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

        if (view is RecyclerView) {
            val myAdapter = AccessoriesRecyclerViewAdapter()
            AccessoriesStore.data.observe(viewLifecycleOwner) {
                myAdapter.replaceValues(it)
                if (!view.isComputingLayout) {
                    myAdapter.notifyDataSetChanged()
                }
            }
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = myAdapter
            }

        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}