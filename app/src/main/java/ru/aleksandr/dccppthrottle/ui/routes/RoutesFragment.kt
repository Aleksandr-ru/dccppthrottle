/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.routes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import ru.aleksandr.dccppthrottle.BuildConfig
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.databinding.FragmentRoutesBinding
import ru.aleksandr.dccppthrottle.dialogs.RouteAddDialog
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.store.MockStore
import ru.aleksandr.dccppthrottle.store.RoutesStore

class RoutesFragment : Fragment() {
    @Suppress("unused")
    private val TAG = javaClass.simpleName

    private var _binding: FragmentRoutesBinding? = null
    private var routeJob: Job? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val view = binding.listRoutes
        val placeholder = binding.emptyView

        placeholder.setOnClickListener {
            if (BuildConfig.DEBUG) for(i in 1..10)
                RoutesStore.add(MockStore.randomRoute())
            else
                RouteAddDialog().show(parentFragmentManager, RouteAddDialog.TAG)
        }

        val rvAdapter = RoutesRecyclerViewAdapter(::runRoute)
        RoutesStore.data.observe(viewLifecycleOwner) {
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

        return root
    }

    private fun runRoute(position: Int) {
        if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        val route = RoutesStore.data.value?.getOrNull(position) ?: return
        // Keep this run independent of subsequent edits to the route's mutable list.
        val accessories = route.accessories.map { it.copy() }
        routeJob?.cancel()
        routeJob = viewLifecycleOwner.lifecycleScope.launch {
            val dialog = ProgressDialog(requireContext()).apply {
                setTitle(route.title)
                setMax(accessories.size)
                setNegativeButton(android.R.string.cancel) { _, _ -> routeJob?.cancel() }
            }
            try {
                dialog.show()
                accessories.forEach { acc ->
                    ensureActive()
                    dialog.setMessage(acc.toString())
                    CommandStation.setAccessoryState(acc.address, acc.isOn)
                    dialog.incrementProgress()
                    delay(acc.delay.toLong())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.w(TAG, e)
                Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
            } finally {
                dialog.dismiss()
            }
        }
    }

    override fun onPause() {
        // ViewPager2 pauses this fragment when another tab becomes selected,
        // even though its view and lifecycleScope may remain alive.
        routeJob?.cancel()
        routeJob = null
        super.onPause()
    }

    override fun onDestroyView() {
        routeJob?.cancel()
        routeJob = null
        binding.listRoutes.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
