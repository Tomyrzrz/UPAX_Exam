package com.softim.moviesapi

import android.Manifest
import android.R.attr.delay
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.softim.moviesapi.databinding.ActivityHomeBinding
import com.softim.moviesapi.models.ModelUserLocation
import com.softim.moviesapi.utilities.ExceptionDialogFragment
import java.util.*


class Home : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val fm: FragmentManager = supportFragmentManager
    private var bd = Firebase.firestore
    private lateinit var ref: DocumentReference
    private val TIEMPO = 300000L
    private var handler = Handler()
    private var runnable: Runnable? = null
    private val CHANNEL_ID = "MOVIESAPI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()
        ubicacion()
        setSupportActionBar(binding.appBarHome.toolbar)

        binding.appBarHome.fab.setOnClickListener { view ->
            Snackbar.make(view, "Thanks For Visit my App", Snackbar.LENGTH_LONG)
                .setAction("Thanks", null).show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_home)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


    private fun ubicacion() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location: Location? ->
                                    mensaje(location)
                                }
                        }
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                mensaje(location)
                            }
                    }
                    else -> {
                        notificar()
                    }
                }
            }
        }
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    override fun onResume() {
        ejecutarTarea()
        super.onResume()
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable!!)
        super.onDestroy()
    }

    private fun ejecutarTarea() {
        handler.postDelayed(Runnable {
            ubicacion()
            handler.postDelayed(runnable!!, TIEMPO)
        }.also { runnable = it }, TIEMPO)
    }

    private fun notificar() {
        val message = "You must be granted the permission of location."
        ExceptionDialogFragment(message).show(fm, ExceptionDialogFragment.TAG)
        ubicacion()
    }

    private fun mensaje(location: Location?) {
        val sharedPreferences = getSharedPreferences("user_movies", MODE_PRIVATE)
        val user_local = sharedPreferences.getString("user", "")
        val uniqueID = UUID.randomUUID().toString()
        if (user_local == "") {
            val editor = sharedPreferences.edit()
            editor.putString("user", uniqueID)
            editor.apply()
            ref = bd.collection("moviesAPIuser").document(uniqueID)
                .collection("locations").document()
        } else {
            ref = bd.collection("moviesAPIuser").document(user_local!!)
                .collection("locations").document()
        }

        val user = ModelUserLocation(uniqueID, location!!.latitude, location.longitude)
        ref.set(user)
            .addOnSuccessListener {
                val message = "Right now you are. \nLatitude: ${location.latitude} \nLongitude: ${location.longitude}"
                notificationLocation(message)
            }.addOnFailureListener {
                val message = "Upload Location Failed"
                notificationLocation(message)
            }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notificationLocation(msj: String){
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Your Location")
            .setContentText(msj)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        with(NotificationManagerCompat.from(this)) {
            notify(2, builder.build())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}