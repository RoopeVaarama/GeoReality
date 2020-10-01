package com.example.georeality

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 24.09.2020
 */

class MapFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
    private lateinit var navController : NavController
    lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map : GoogleMap
    private lateinit var fragmentTransaction: FragmentTransaction
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
        map.setOnMarkerClickListener { marker ->
            var startPoint = lastLocation
            var endPoint: Location = Location(LocationManager.GPS_PROVIDER)
            endPoint.latitude = marker.position.latitude
            endPoint.longitude = marker.position.longitude
            var dist = startPoint.distanceTo(endPoint)

            if (dist < 15.0) {
                Log.d("marker", "onclick in range")
                Log.d("marker", "distance to marker: ${dist} meters")
                navController.navigate(R.id.entityFragment)
            } else {
                marker.showInfoWindow()
                Toast.makeText(
                    requireActivity().applicationContext,
                    "You are ${dist} meters away from the marker you have to be less than 15 meters away to open cachegit",
                    Toast.LENGTH_LONG).show()
                Log.d("marker", "onclick")
                Log.d("marker", "distance to marker: ${dist} meters")
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
            Toast.makeText(
                requireActivity().applicationContext,
                "Current location:${lastLocation.latitude} ${lastLocation.longitude}",
                Toast.LENGTH_LONG
            ).show()
            val locationdata = "${lastLocation.latitude}, ${lastLocation.longitude}"
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
            var editor = sharedPref?.edit()
            editor?.putString("locationData", locationdata)
            editor?.commit()

            navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_mapFragment_to_cacheCreationFragment)
        }
    }
    private fun loadEntitys(){
        dbViewModel = Database.dbViewModel
        dbViewModel!!.audioMarkers.observe(viewLifecycleOwner, Observer {
            Log.d("onCreateView", it.toString())
            //To create audio markers on map
            for (i in it.indices){
                Log.d("marker", "${it[i].latitude!!} ${it[i].longitude}" )
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(it[i].latitude!!, it[i].longitude!!))
                        .title(it[i].title)
                )}

        })
        dbViewModel!!.arMarkers.observe(viewLifecycleOwner, Observer {
            Log.d("OnCreateView", it.toString())
            //Create AR markers on map
            for (i in it.indices){
                Log.d("marker", "${it[i].latitude!!} ${it[i].longitude}" )
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(it[i].latitude!!, it[i].longitude!!))
                        .title(it[i].title)
                        .position(LatLng(60.2314768, 24.969129))
                        .title("Testi marker")
                        /*.position(LatLng(it[i].latitude!!, it[i].longitude!!))
                        .title(it[i].title)*/
                )}
        })
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireActivity().applicationContext, "MyLocation button clicked", Toast.LENGTH_LONG).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(requireActivity().applicationContext, "Current locatin:\n$location", Toast.LENGTH_LONG).show()

    }

}