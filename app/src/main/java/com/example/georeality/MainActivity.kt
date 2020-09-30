package com.example.georeality

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.onNavDestinationSelected
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 22.09.2020
 */

/**
 * MainActivity is the base activity, which contains the top navigation and map functionality
 */

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var navController : NavController
    private lateinit var mAuth: FirebaseAuth
    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var fragmentTransaction: FragmentTransaction
    private var user: FirebaseUser? = null
    private val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        //Check if user is logged in, if not then go to login screen
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser

        if (!userIsLoggedIn()) {
            showSignInOptions()
        } else {
            Log.d("User", user!!.email!!)
        }
        setupNav()
        checkPermissions()
        loadMarkers()

    }

    private fun checkPermissions() {
        if ((Build.VERSION.SDK_INT >= 26 && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    private fun loadMarkers(){

    }

    private fun switchToMapFragment() {
        if(navController.currentDestination!!.id != R.id.mapFragment) {
            navController.navigate(R.id.mapFragment)
        }

    }

    private fun switchToUserCachesFragment() {
        navController.navigate(R.id.myCachesFragment)

    }

    private fun switchToInfoFragment() {
        navController.navigate(R.id.infoFragment)

    }
    private fun userIsLoggedIn(): Boolean {
        return user != null
    }

    fun isUserAnonymous(): Boolean {
        return user!!.isAnonymous
    }

    private fun showSignInOptions() {
        providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .build(),
            REQUEST_CODE
        )
    }

    /**
     * Set up the top app bar and related navigation
     * ============================================
     */
    private fun setupNav() {
        setSupportActionBar(topAppBar)


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
            "Map" -> {
                switchToMapFragment()
                navigation.setCheckedItem(item)
            }
            "Logout" -> {
                mAuth.signOut()
                showSignInOptions()
            }
            "My caches" -> switchToUserCachesFragment()
            "Info" -> switchToInfoFragment()
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }
}
