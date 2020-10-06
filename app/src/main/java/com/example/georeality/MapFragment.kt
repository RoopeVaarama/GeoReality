package com.example.georeality

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 24.09.2020
 */

/**
 * MapFragment includes all Google Maps related implementation
 */
class MapFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
    private lateinit var navController : NavController
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map : GoogleMap
    private var dbViewModel : DBViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener(fabClickListener)
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()
        loadEntitys()
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(requireActivity()))
        map.setOnInfoWindowClickListener { marker ->
            val startPoint = lastLocation
            val endPoint = Location(LocationManager.GPS_PROVIDER)
            endPoint.latitude = marker.position.latitude
            endPoint.longitude = marker.position.longitude
            val dist = startPoint.distanceTo(endPoint)

            if (dist < 15.0) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.cache_dialog_title)
                    .setMessage(resources.getString(R.string.cache_dialog_message))
                    .setNegativeButton(resources.getString(R.string.cache_dialog_cancel)) { _, _ ->
                        return@setNegativeButton
                    }
                    .setPositiveButton(resources.getString(R.string.cache_dialog_open)) { _, _ ->
                        val gson = Gson()
                        if (marker.tag is ARMarker) {
                            val markerClass = marker.tag as ARMarker
                            val markerJsonString = gson.toJson(markerClass)
                            Log.d("Tags", markerClass.toString())
                            val action = MapFragmentDirections.actionMapFragmentToArFragment(markerJsonString)
                            navController.navigate(action)
                        } else if (marker.tag is AudioMarker) {
                            val markerClass = marker.tag as AudioMarker
                            val markerJsonString = gson.toJson(markerClass)
                            Log.d("Tags", markerJsonString)
                            val action = MapFragmentDirections.actionMapFragmentToAudioListeningFragment(markerJsonString)
                            navController.navigate(action)
                        }
                    }
                    .show()
            }
        }
        map.setOnMarkerClickListener { marker ->
            val startPoint = lastLocation
            val endPoint = Location(LocationManager.GPS_PROVIDER)
            endPoint.latitude = marker.position.latitude
            endPoint.longitude = marker.position.longitude
            val dist = startPoint.distanceTo(endPoint)

            if (dist < 15.0) {
                Log.d("marker", "onclick in range")
                Log.d("marker", "distance to marker: $dist meters")
                marker.snippet = "Click here to open cache!"
                marker.showInfoWindow()
            } else {
                marker.snippet = "Too far away!"
                marker.showInfoWindow()
                Log.d("marker", "onclick")
                Log.d("marker", "distance to marker: $dist meters")
            }
            true
        }
    }

    //Enables My Location layer if the fine location permission has been granted.
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            //Permission to access the location is missing. Show rationale and request permission
            if ((Build.VERSION.SDK_INT >= 26 && ContextCompat.checkSelfPermission(
                    requireActivity().applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    //Fab listener for adding markers and entitys
    @SuppressLint("MissingPermission")
    private val fabClickListener = View.OnClickListener {
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
            /*Toast.makeText(
                requireActivity().applicationContext,
                "Current location:${lastLocation.latitude} ${lastLocation.longitude}",
                Toast.LENGTH_LONG
            ).show()*/
            val locationdata = "${lastLocation.latitude}, ${lastLocation.longitude}"
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref?.edit()
            editor?.putString("locationData", locationdata)
            editor?.commit()

            navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_mapFragment_to_cacheCreationFragment)
        }
    }
    private fun loadEntitys(){
        dbViewModel = Database.dbViewModel

        dbViewModel!!.audioMarkers.observe(viewLifecycleOwner, {
            Log.d("onCreateView", it.toString())
            // Create audio markers on map
            for (i in it.indices){
                Log.d("marker", "${it[i].latitude!!} ${it[i].longitude}" )
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(it[i].latitude!!, it[i].longitude!!))
                        .title(it[i].title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .snippet("${it[i].creator} \n asdasd")
                ).tag = it[i]
            }

        })
        dbViewModel!!.arMarkers.observe(viewLifecycleOwner, {
            Log.d("OnCreateView", it.toString())
            // Create AR markers on map
            for (i in it.indices){
                Log.d("marker", "${it[i].latitude!!} ${it[i].longitude}" )
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(it[i].latitude!!, it[i].longitude!!))
                        .title(it[i].title)
                        .snippet("${it[i].creator} \n asdasd")
                ).tag = it[i]
            }
        })
    }

    override fun onMyLocationButtonClick(): Boolean {
        //Toast.makeText(requireActivity().applicationContext, "MyLocation button clicked", Toast.LENGTH_LONG).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        //Toast.makeText(requireActivity().applicationContext, "Current locatin:\n$location", Toast.LENGTH_LONG).show()

    }

}