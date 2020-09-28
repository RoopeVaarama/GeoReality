package com.example.georeality

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_cache_creation.*
import kotlinx.android.synthetic.main.fragment_cache_creation.view.*

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 24.09.2020
 */
class CacheCreationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cache_creation, container, false)
        val spinner: Spinner = view.findViewById(R.id.spinner)

        spinner.onItemSelectedListener = SpinnerActivity()
        //Crate an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.type_array,
            R.layout.color_spinner_layout
        ).also { adapter ->
            //Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            //Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        return view
    }

}

class SpinnerActivity: Activity(), AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        parent?.getItemAtPosition(pos)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}