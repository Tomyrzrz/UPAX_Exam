package com.softim.moviesapi.ui.fragments

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.softim.moviesapi.R
import com.softim.moviesapi.data.models.ModelUser

class MapsFragment : Fragment() {
    private var longitud: Double = 0.0
    private var latitud: Double = 0.0
    private var nombre: String = ""
    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.uiSettings.isMyLocationButtonEnabled = true
        /*val sharedPreferences = activity?.getSharedPreferences("user_movies", AppCompatActivity.MODE_PRIVATE)
        val user_local = sharedPreferences?.getString("user", "")*/
        /*bd.collection("moviesAPIuser").document(user_local!!).collection("locations").get()
            .addOnSuccessListener {
                if (it != null){*/

            val ubica = LatLng(latitud, longitud)
            googleMap.addMarker(
                MarkerOptions().flat(true)
                    .position(ubica)
                    .title(nombre)
                    .alpha(0.8f)
                    .flat(true)
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(ubica))

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (getArguments() != null) {
            longitud = requireArguments().getDouble("longitud")
            latitud = requireArguments().getDouble("latitud")
            nombre = requireArguments().getString("nombre")!!
        }else {
            longitud = 0.0
            latitud = 0.0
            nombre = "Nothing"
        }
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}