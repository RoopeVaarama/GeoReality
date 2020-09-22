package com.example.georeality

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import android.Manifest
import android.location.Location
import android.widget.Toast
import com.google.android.gms.maps.OnMapReadyCallback

class MainActivity : AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback  {

    private lateinit var map: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Map fragment
        //val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        //mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?){
        map = googleMap ?: return
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()
    }

    //Enables My Location layer if the fine location permission has been granted.
    private fun enableMyLocation(){
        if(!::map.isInitialized) return
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            map.isMyLocationEnabled = true
        } else {
            //Permission to access the location is missing. Show rationale and request permission
            if((Build.VERSION.SDK_INT  >= 26 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean{
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_LONG).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current locatin:\n$location", Toast.LENGTH_LONG).show()
    }
}