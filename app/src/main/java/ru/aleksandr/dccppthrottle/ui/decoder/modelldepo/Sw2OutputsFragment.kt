/*
 * Copyright (c) 2025. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.ui.decoder.modelldepo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.ui.decoder.DecoderFragment

class Sw2OutputsFragment : DecoderFragment() {
    private val TAG = javaClass.simpleName

    private val model by activityViewModels<Sw2OutputsViewModel>()
    private lateinit var rvAdapter: Sw2OutputsRecyclerViewAdapter

    private lateinit var emptyView: TextView
    private lateinit var listView: RecyclerView
    private lateinit var writeButton: Button

    companion object {
        fun newInstance() = Sw2OutputsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_sw2_outputs, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            emptyView = findViewById(R.id.textEmpty)
            listView = findViewById(R.id.rvOutputs)
            writeButton = findViewById(R.id.buttonWrite)
        }

        emptyView.setOnClickListener {
            readModelCVs(model, MANUFACTURER_ID_MODELLDEPO)
        }

        rvAdapter = Sw2OutputsRecyclerViewAdapter(model, viewLifecycleOwner)
        with(listView) {
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
        }

        writeButton.setOnClickListener {
            val job = writeChangedCVs(model)
            job.invokeOnCompletion {
                if (!job.isCancelled) activity?.onBackPressed()
            }
        }

        model.hasChanges.observe(viewLifecycleOwner) {
            writeButton.isEnabled = it
        }

        model.loaded.observe(viewLifecycleOwner) {
            if (it) {
                emptyView.visibility = View.GONE
                listView.visibility = View.VISIBLE
                writeButton.visibility = View.VISIBLE
            }
            else {
                emptyView.visibility = View.VISIBLE
                listView.visibility = View.GONE
                writeButton.visibility = View.GONE
            }
        }

        if ((savedInstanceState == null) && (model.loaded.value == true)) {
            Snackbar.make(view, R.string.message_cvs_outdated, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_reload) {
                    readModelCVs(model, MANUFACTURER_ID_MODELLDEPO)
                }.show()
        }
    }
}