package ru.aleksandr.dccppthrottle.ui.accessories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.databinding.FragmentAccessoriesBinding
import ru.aleksandr.dccppthrottle.store.AccessoriesStore
import ru.aleksandr.dccppthrottle.store.MockStore

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
        val placeholder = binding.emptyView

        placeholder.setOnClickListener {
            for(i in 1..10) {
                try {
                    AccessoriesStore.add(MockStore.randomAccessory())
                }
                catch (ex: Exception) {
                    Log.d("Exception", ex.toString())
                }
            }
        }

        if (view is RecyclerView) {
            val rvAdapter = AccessoriesRecyclerViewAdapter(parentFragmentManager)
            AccessoriesStore.data.observe(viewLifecycleOwner) {
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