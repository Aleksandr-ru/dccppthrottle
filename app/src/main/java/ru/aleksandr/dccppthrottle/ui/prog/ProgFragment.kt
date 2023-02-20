package ru.aleksandr.dccppthrottle.ui.prog

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ToggleButton
import ru.aleksandr.dccppthrottle.R
import ru.aleksandr.dccppthrottle.view.PlusMinusView

class ProgFragment : Fragment() {

    private lateinit var viewBits: Array<ToggleButton>

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

        val viewCvVal = view.findViewById<PlusMinusView>(R.id.plusminusCvValue)

        viewBits = Array<ToggleButton>(8) { i ->
            ToggleButton(view.context, null, R.style.Widget_Theme_DCCppThrottle_Toggle_Bit).apply {
                text = i.toString()
                textOn = text
                textOff = text
                tag = i
                setOnCheckedChangeListener { button, isChecked ->
                    if (button.isPressed) {
                        viewCvVal.value = bitsToInt()
                    }
                }
            }
        }

        val layoutBits: LinearLayout = view.findViewById(R.id.layoutBits)
        for(i in 7..0) {
            layoutBits.addView(viewBits[i])
        }

        viewCvVal.onChangeListener = {
            it?.let {
                valueToBits(it)
            }
        }
    }

    private fun valueToBits(value: Int) {
        val bin = value.toUByte().toString(2)
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