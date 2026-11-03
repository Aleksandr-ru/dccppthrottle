package ru.aleksandr.dccppthrottle.ui.prog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.dialogs.ProgressDialog
import ru.aleksandr.dccppthrottle.view.AdjustableCurveView
import ru.aleksandr.dccppthrottle.view.LockableScrollView
import ru.aleksandr.dccppthrottle.view.PlusMinusView

import ru.aleksandr.dccppthrottle.store.SpeedCurveStore

class SpeedCurveFragment : ProgrammingFragment() {
    private val model by activityViewModels<SpeedCurveViewModel>()
    private val extended get() = arguments?.getBoolean("extended") ?: false
    private var operation: Job? = null
    private var rendering = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_speed_curve, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pointCvs = SpeedCurveStore.pointCvs(extended)
        val dataCvs = SpeedCurveStore.dataCvs(extended)
        val graph = view.findViewById<AdjustableCurveView>(R.id.curveGraph)
        val mode = view.findViewById<SwitchMaterial>(R.id.curveMode)
        val smoothingToggle = view.findViewById<SwitchMaterial>(R.id.curveSmoothing)
        smoothingToggle.visibility = if (extended) View.VISIBLE else View.GONE
        fun updateSmoothing() {
            val radius = if (extended && model.smoothingEnabled.value == true) 8 else 0
            if (graph.smoothing != radius) graph.smoothing = radius
        }
        smoothingToggle.setOnCheckedChangeListener { _, enabled ->
            if (!rendering && extended) model.setSmoothingEnabled(enabled)
        }
        val rows = view.findViewById<LinearLayout>(R.id.curveRows)
        val trimRows = view.findViewById<LinearLayout>(R.id.curveTrimRows)
        view.findViewById<View>(R.id.curveTrimSection).visibility = if (extended) View.VISIBLE else View.GONE
        val read = view.findViewById<Button>(R.id.curveRead)
        val write = view.findViewById<Button>(R.id.curveWrite)
        val inputs = dataCvs.associateWith { cv ->
            val parent = if (cv == 66 || cv == 95) trimRows else rows
            val row = layoutInflater.inflate(R.layout.view_speed_curve_cv, parent, false)
            val label = row.findViewById<TextView>(R.id.curveCvLabel)
            label.text = when (cv) {
                2 -> getString(R.string.label_prog_cv2)
                6 -> getString(R.string.label_prog_cv6)
                5 -> getString(R.string.label_prog_cv5)
                66 -> getString(R.string.label_prog_cv66)
                95 -> getString(R.string.label_prog_cv95)
                else -> getString(R.string.label_prog_speed_step_cv_xx, cv, cv - 66)
            }
            if (cv == 66 || cv == 95) {
                row.findViewById<TextView>(R.id.curveCvDescription).apply {
                    setText(if (cv == 66) R.string.label_prog_cv66_desc else R.string.label_prog_cv95_desc)
                    visibility = View.VISIBLE
                }
            }
            parent.addView(row)
            row.findViewById<PlusMinusView>(R.id.curveCvInput).apply {
                tag = cv
                contentDescription = label.text
                setOnChangeListener { value ->
                    if (!rendering && value != null) model.setValues(mapOf(cv to value))
                }
            }
        }
        mode.setText(if (extended) R.string.label_prog_extended_speed_curve_enable else R.string.label_prog_simple_speed_curve_enable)
        mode.setOnCheckedChangeListener { _, checked ->
            if (!rendering) model.setSimpleEnabled(if (extended) !checked else checked)
        }
        graph.setOnChangeListener { values ->
            model.setValues(pointCvs.zip(values).toMap())
        }
        graph.setOnDragStateChangeListener { dragging ->
            val scroll = view as? LockableScrollView ?: view.findViewById(R.id.curveScroll)
            scroll.setScrollingEnabled(!dragging)
            requireActivity().findViewById<ViewPager2>(R.id.progPager).isUserInputEnabled = !dragging
        }
        model.values.observe(viewLifecycleOwner) { values ->
            rendering = true
            try {
                inputs.forEach { (cv, input) ->
                    val value = values.getValue(cv)
                    if (input.value != value) input.value = value
                }
                val points = pointCvs.map { values.getValue(it) }
                // Reassigning points ends a drag; avoid doing so for the graph's own edits.
                if (graph.points != points) graph.points = points
                updateSmoothing()
            } finally { rendering = false }
        }
        model.simpleEnabled.observe(viewLifecycleOwner) { enabled ->
            rendering = true
            try { mode.isChecked = if (extended) !enabled else enabled }
            finally { rendering = false }
        }
        model.smoothingEnabled.observe(viewLifecycleOwner) { enabled ->
            rendering = true
            try {
                smoothingToggle.isChecked = enabled
                updateSmoothing()
            } finally { rendering = false }
        }
        model.busy.observe(viewLifecycleOwner) { busy ->
            graph.isEnabled = !busy
            mode.isEnabled = !busy
            smoothingToggle.isEnabled = !busy
            inputs.values.forEach { it.isEnabled = !busy }
            read.isEnabled = !busy
            write.isEnabled = !busy
        }
        read.setOnClickListener { transfer(write = false) }
        write.setOnClickListener { transfer(write = true) }
    }

    private fun transfer(write: Boolean) {
        if (model.busy.value == true) return
        val dataCvs = SpeedCurveStore.dataCvs(extended)
        val snapshot = model.values.value!!.toMap()
        val useSimple = model.simpleEnabled.value!!
        val readCvs = SpeedCurveStore.readCvs(extended)
        val dialog = ProgressDialog(requireContext()).apply {
            if (write) setTitle(R.string.title_dialog_writing_cvs)
            else setTitle(getString(R.string.title_dialog_reading_x_cvs, readCvs.size))
            setMax(if (write) dataCvs.size + 2 else readCvs.size)
            setNegativeButton(android.R.string.cancel) { _, _ -> operation?.cancel() }
            show()
        }
        model.busy.value = true
        operation = viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (write) {
                    // A fresh read is mandatory: preserve every CV29 bit except speed-table selection.
                    dialog.setMessage(getString(R.string.message_prog_cv_progress, 29))
                    val cv29 = SpeedCurveStore.withSimpleEnabled(readProgrammingCv(29), useSimple)
                    dialog.incrementProgress()
                    for (cv in dataCvs) {
                        dialog.setMessage(getString(R.string.message_prog_cv_progress, cv))
                        writeProgrammingCv(cv, snapshot.getValue(cv))
                        dialog.incrementProgress()
                    }
                    // Select the requested mode only after all curve values have been written.
                    dialog.setMessage(getString(R.string.message_prog_cv_progress, 29))
                    writeProgrammingCv(29, cv29)
                    model.setValues(mapOf(29 to cv29))
                    dialog.incrementProgress()
                } else {
                    val values = mutableMapOf<Int, Int>()
                    for (cv in readCvs) {
                        dialog.setMessage(getString(R.string.message_prog_cv_progress, cv))
                        values[cv] = readProgrammingCv(cv)
                        dialog.incrementProgress()
                    }
                    // An interrupted read leaves the edited curve intact.
                    model.applyRead(values)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Toast.makeText(context, error.message ?: error.toString(), Toast.LENGTH_LONG).show()
            } finally {
                dialog.dismiss()
                model.busy.value = false
                operation = null
            }
        }
    }

    override fun onDestroyView() {
        operation?.cancel()
        view?.findViewById<AdjustableCurveView>(R.id.curveGraph)?.apply {
            setOnChangeListener(null)
            setOnDragStateChangeListener(null)
        }
        activity?.findViewById<ViewPager2>(R.id.progPager)?.isUserInputEnabled = true
        super.onDestroyView()
    }

    companion object {
        fun newInstance(extended: Boolean) = SpeedCurveFragment().apply {
            arguments = Bundle().apply { putBoolean("extended", extended) }
        }
    }
}
