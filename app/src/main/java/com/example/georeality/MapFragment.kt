package com.example.georeality

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
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
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 24.09.2020
 */

class MapFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
    private lateinit var navController : NavController
    private  lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map : GoogleMap
    private lateinit var fragmentTransaction: FragmentTransaction
    private var dbViewModel : DBViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!.applicationContext)
        dbViewModel = DBViewModel()
        dbViewModel!!.audioMarkers.observe(viewLifecycleOwner, Observer {
            Log.d("onCreateView", it.toString())
        })

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        fab.setOnClickListener{testFun(view)}
    }
    fun testFun(view : View) {
        navController = Navigation.findNavController(view)
        navController.navigate(R.id.action_mapFragment_to_cacheCreationFragment)
        dbViewModel!!.addNewAudioMarker("asds", 1.0, 3.0, "asd")
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()

    }

    //Enables My Location layer if the fine location permission has been granted.
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            //Permission to access the location is missing. Show rationale and request permission
            if ((Build.VERSION.SDK_INT >= 26 && ContextCompat.checkSelfPermission(
                    activity!!.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->
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
        fusedLocationClient.lastLocation.addOnSuccessListener(activity!!) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
            Toast.makeText(
                activity!!.applicationContext,
                "Current location:${lastLocation.latitude} ${lastLocation.longitude}",
                Toast.LENGTH_LONG
            ).show()

            fragmentTransaction = childFragmentManager.beginTransaction()
            val mapFragment = MapFragment()
            //fragmentTransaction.replace(R.id.fragment_container, mapFragment)
            fragmentTransaction.commit()
            /*val intent = Intent(this, EntityActivity::class.java).apply {
                putExtra("latitude", lastLocation.latitude)
                putExtra("longitude", lastLocation.longitude)
            }
            startActivity(intent)*/
        }
    }

    private fun createEntity(location: Location){
        Toast.makeText(activity!!.applicationContext, "Current locatin:\n$location for new entity", Toast.LENGTH_LONG).show()
        map.addMarker(
            MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))
                .title("Created by (current username)")

        )
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(activity!!.applicationContext, "MyLocation button clicked", Toast.LENGTH_LONG).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(activity!!.applicationContext, "Current locatin:\n$location", Toast.LENGTH_LONG).show()

    }

}