package com.softim.moviesapi.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.softim.moviesapi.R
import com.softim.moviesapi.models.ModelUserLocation

class MapsFragment : Fragment() {
    private val bd = Firebase.firestore
    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.uiSettings.isMyLocationButtonEnabled = true
        val sharedPreferences = activity?.getSharedPreferences("user_movies", AppCompatActivity.MODE_PRIVATE)
        val user_local = sharedPreferences?.getString("user", "")
        bd.collection("moviesAPIuser").document(user_local!!).collection("locations").get()
            .addOnSuccessListener {
                if (it != null){
                    val locations = it.toObjects<ModelUserLocation>()
                    for (doc in locations){
                        val ubica = LatLng(doc.latitude, doc.longitude)
                        googleMap.addMarker(
                            MarkerOptions().flat(true)
                                .position(ubica)
                                .title(doc.time.toString())
                                .alpha(0.8f)
                                .flat(true))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ubica))
                    }
                }
            }



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}