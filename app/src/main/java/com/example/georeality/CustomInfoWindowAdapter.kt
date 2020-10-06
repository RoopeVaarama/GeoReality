package com.example.georeality

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/**
 * InfoWindow adapter. Replaces default Google Maps marker info window with a customized one.
 */
class CustomInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {
    @SuppressLint("InflateParams")
    private val mWindow : View =
        LayoutInflater.from(context).inflate(R.layout.maps_custom_info_window, null)
    private val mContext: Context = context
    private var arMarker : ARMarker? = null
    private var audioMarker : AudioMarker? = null

    private fun renderWindowText(marker : Marker, view : View) {
        val titleView : TextView = view.findViewById(R.id.markerTitleText)
        val typeView : TextView = view.findViewById(R.id.markerTypeText)
        val creatorView : TextView = view.findViewById(R.id.markerCreatorText)
        val distanceView : TextView = view.findViewById(R.id.markerDistanceText)

        if (marker.tag is ARMarker) {
            arMarker = marker.tag as ARMarker
            titleView.text = arMarker!!.title
            typeView.text = mContext.getString(R.string.ar)
            creatorView.text = arMarker!!.creator
        } else if (marker.tag is AudioMarker) {
            audioMarker = marker.tag as AudioMarker
            titleView.text = audioMarker!!.title
            typeView.text = mContext.getString(R.string.audio)
            creatorView.text = audioMarker!!.creator
        }
        distanceView.text = marker.snippet


    }
    override fun getInfoWindow(marker:Marker):View {
        renderWindowText(marker, mWindow)
        return mWindow
    }
    override fun getInfoContents(marker:Marker):View {
        renderWindowText(marker, mWindow)
        return mWindow
    }
}