package com.example.georeality

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import android.Manifest
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.OnMapReadyCallback
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 22.09.2020
 */

/**
 * MainActivity is the base activity, which contains the top navigation and map functionality
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
    private lateinit var mAuth : FirebaseAuth
    private lateinit var map: GoogleMap
    private lateinit var providers : List<AuthUI.IdpConfig>
    private var user : FirebaseUser? = null
    private val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Check if user is logged in, if not then go to login screen
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
        if (!userIsLoggedIn()) {
            showSignInOptions()
        } else {
            Log.d("User", user!!.email!!)
            setupNav()

            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(this)
        }
    }

    private fun userIsLoggedIn() : Boolean {
        return user != null
    }

    private fun showSignInOptions() {
        providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build()
        )
        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .build(),
            REQUEST_CODE)
    }

    /**
     * Set up the top app bar and related navigation
     * ============================================
     */
    private fun setupNav() {
        setSupportActionBar(topAppBar)

        navigation.apply {  }
        val drawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            topAppBar,
            R.string.app_name,
            R.string.app_name
        )
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        navigation.setNavigationItemSelectedListener(this)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("Item", item.toString())
        when (item.toString()) {
            "Map" -> Log.d("Pressed", item.toString())
            "My caches" -> Log.d("Pressed", item.toString())
            "Create new cache" -> Log.d("Pressed", item.toString())
            "Logout" -> {
                mAuth.signOut()
                showSignInOptions()
            }
        }
        return false
    }
    /**
     * ============================================
     */

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()
    }

    //Enables My Location layer if the fine location permission has been granted.
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            //Permission to access the location is missing. Show rationale and request permission
            if ((Build.VERSION.SDK_INT >= 26 && ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_LONG).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current locatin:\n$location", Toast.LENGTH_LONG).show()
    }
}
