package ru.aleksandr.dccppthrottle.ui.locomotives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.aleksandr.dccppthrottle.databinding.FragmentLocoListBinding as FragmentLocomotivesBinding
import ru.aleksandr.dccppthrottle.placeholder.PlaceholderContent

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
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = LocoRecyclerViewAdapter(PlaceholderContent.ITEMS)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}