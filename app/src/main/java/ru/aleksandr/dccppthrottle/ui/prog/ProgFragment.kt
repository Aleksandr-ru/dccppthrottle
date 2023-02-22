package ru.aleksandr.dccppthrottle.ui.prog

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.cs.CommandStation
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class ProgFragment : Fragment() {

    private lateinit var viewBits: Array<ToggleButton>
    private val model by activityViewModels<ProgViewModel>()

    companion object {
        fun newInstance() = ProgFragment()
    }

    private lateinit var viewModel: ProgViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProgViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_prog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val viewCvNum = view.findViewById<PlusMinusView>(R.id.plusminusCvNum)
        viewCvNum.setOnChangeListener {
            it?.let {
                model.setCvNum(it)
            }
        }
        model.cvNum.observe(viewLifecycleOwner) {
            viewCvNum.value = it
        }

        val viewCvVal = view.findViewById<PlusMinusView>(R.id.plusminusCvValue)
        viewCvVal.setOnChangeListener {
            it?.let {
                model.setCvVal(it)
                valueToBits(it)
            }
        }
        model.cvVal.observe(viewLifecycleOwner) {
            viewCvVal.value = it
        }

        val layoutBits: LinearLayout = view.findViewById(R.id.layoutBits)
        // https://stackoverflow.com/questions/52508070/how-to-dynamically-add-buttons-to-view-so-that-layout-width-works-correctly
        viewBits = Array<ToggleButton>(8) { i ->
            val bit = layoutInflater.inflate(R.layout.bit_toggle, layoutBits, false) as ToggleButton
            layoutBits.addView(bit, 0)
            bit.apply {
                text = i.toString()
                textOn = text
                textOff = text
                tag = i
                setOnCheckedChangeListener { button, _ ->
                    if (button.isPressed) {
                        viewCvVal.value = bitsToInt()
                        viewCvVal.requestFocus()
                    }
                }
            }
        }

        val buttonRead = view.findViewById<Button>(R.id.buttonRead)
        val buttonWrite = view.findViewById<Button>(R.id.buttonWrite)
        buttonRead.setOnClickListener {
            val cv = viewCvNum.value
            cv?.let {
                CommandStation.getCvProg(it) { cv, value ->
                    var message = ""
                    if (value >= 0) {
                        if (viewCvNum.value == cv) {
                            viewCvVal.value = value
                        }
                        val format = getString(R.string.message_read_cv_value)
                        message = String.format(format, cv, value)
                    }
                    else {
                        val format = getString(R.string.message_read_cv_error)
                        message = String.format(format, cv)
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        buttonWrite.setOnClickListener {
            val cv = viewCvNum.value
            val value = viewCvVal.value
            cv?.let {
                value?.let {
                    CommandStation.setCvProg(cv, value) { cv, value ->
                        val stringId =
                            if (value < 0) R.string.message_write_cv_error
                            else R.string.message_write_cv_success
                        val format = getString(stringId)
                        val message = String.format(format, cv)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun valueToBits(value: Int) {
        val bin = value.toUByte().toString(2).padStart(8, '0')
        bin.reversed().withIndex().forEach { (index, value) ->
            viewBits[index].isChecked = value == '1'
        }
    }

    private fun bitsToInt() : Int {
        val str = viewBits.map {
            if (it.isChecked) '1' else '0'
        }.joinToString("").reversed()
        return Integer.parseInt(str, 2)
    }
}