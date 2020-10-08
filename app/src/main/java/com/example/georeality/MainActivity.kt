package com.example.georeality

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_header_layout.view.*

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 22.09.2020
 */

/**
 * MainActivity is the base activity, which contains the top navigation and map functionality.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var mAuth: FirebaseAuth
    private lateinit var providers: List<AuthUI.IdpConfig>
    private var user: FirebaseUser? = null
    private val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        //Check if user is logged in, if not then go to login screen
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser

        if (!userIsLoggedIn()) {
            showSignInOptions()
        }
        setupNav()
        checkPermissions()
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
     */
    private fun setupNav() {
        appBarConfiguration = AppBarConfiguration(setOf(R.id.myCachesFragment, R.id.mapFragment, R.id.infoFragment), drawer_layout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigation.setupWithNavController(navController)
        toolbar.setNavigationOnClickListener {
            navController.navigateUp(appBarConfiguration)
        }
        navigation.setNavigationItemSelectedListener(this)
        navigation.menu.findItem(R.id.userEmail).title = user!!.email
    }

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onNavigateUp()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("item", item.toString())
        if (item.itemId == R.id.logoutItem) {
            showSignInOptions()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("menuitem", item.toString())
        when (item.itemId) {
            R.id.mapFragment -> switchToMapFragment()
            R.id.myCachesFragment -> switchToUserCachesFragment()
            R.id.infoFragment -> switchToInfoFragment()
            R.id.logoutItem -> showSignInOptions()
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
