package com.example.georeality

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_info.*

/**
 * @author Roope Vaarama, Topias Peiponen
 * @since 01.10.2020
 */

class InfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set images
        info_fragment_red_img.setImageResource(R.drawable.google_maps_marker_red)
        info_fragment_blue_img.setImageResource(R.drawable.google_maps_marker_blue)
        info_fragment_infowindow_img.setImageResource(R.drawable.georeality_infowindow_example)
    }
}