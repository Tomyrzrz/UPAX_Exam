package com.softim.moviesapi.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.softim.moviesapi.R
import com.softim.moviesapi.databinding.FragmentHomeBinding
import com.softim.moviesapi.data.models.ModelUserLocation
import com.softim.moviesapi.data.models.Model_movie
import com.softim.moviesapi.data.network.APIservice
import com.softim.moviesapi.utilities.ExceptionDialogFragment
import com.softim.moviesapi.utilities.MoviesAdapter
import com.softim.moviesapi.utilities.MoviesLocalBD
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class HomeFragment : Fragment(), AdapterView.OnItemSelectedListener  {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: MoviesAdapter
    private val moviesImages = mutableListOf<Model_movie>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var bd = Firebase.firestore
    private lateinit var ref: DocumentReference
    private val TIEMPO = 300000L
    private val CHANNEL_ID = "MOVIESAPI"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        createNotificationChannel()
        ubicacion()
        ejecutarTarea()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()

        binding.spnMovies.onItemSelectedListener = this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView() {
        adapter = MoviesAdapter(moviesImages)
        binding.rvMovies.layoutManager = LinearLayoutManager(requireContext())
            .apply {
                binding.rvMovies.layoutManager = this
            }
        binding.rvMovies.adapter = adapter
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/movie/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }

        return false
    }

    private fun searchByName(query:String){
        if (isNetworkAvailable(requireContext())) {
            CoroutineScope(Dispatchers.IO).launch {
                val call = getRetrofit().create(APIservice::class.java)
                    .getMoviesByBreeds("$query?api_key=e8a82c4e3f0e112131ea103fade4eb93")
                val list_movies = call.body()

                activity?.runOnUiThread {
                    if (call.isSuccessful) {
                        val pelis = list_movies?.movies ?: emptyList()
                        val admin = MoviesLocalBD(requireContext(),"movies_local", null, 1)
                        val bd = admin.writableDatabase
                        for (pel in pelis){
                            val registro = ContentValues()
                            registro.put("title", pel.title)
                            registro.put("overview", pel.overview)
                            registro.put("poster_path", pel.poster_path)
                            registro.put("vote_average", pel.vote_average)
                            bd.insert("peliculas", null, registro)
                        }
                        bd.close()
                        moviesImages.clear()
                        moviesImages.addAll(pelis)
                        adapter.notifyDataSetChanged()
                    } else {
                        val message = "We have an error"
                        ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
                    }
                }
            }
        }else{
            val admin = MoviesLocalBD(requireContext(),"movies_local", null, 1)
            val bd = admin.writableDatabase
            val fila = bd.rawQuery("select title, overview, poster_path, vote_average from peliculas", null)
            if (fila.moveToFirst()) {
                do {
                    val title: String = fila.getString(0)
                    val overview: String = fila.getString(1)
                    val poster_path: String = fila.getString(2)
                    val vote_average: Double = fila.getDouble(3)
                    val movie = Model_movie(title, overview, poster_path, vote_average)
                    moviesImages.add(movie)
                    adapter.notifyDataSetChanged()
                } while (fila.moveToNext())
            }else{
                val message = "No Network. Connect to Internet"
                ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
            }
            bd.close()
        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val texto = p0?.selectedItem.toString()
        searchByName(texto.lowercase(Locale.ROOT))
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        val message = "Select Breed of Dog."
        ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
    }


    private fun ubicacion() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                requireContext(),
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

    @SuppressLint("MissingPermission")
    private fun reubicate(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                mensaje(location)
            }
    }


    private fun ejecutarTarea() {
        GlobalScope.launch(Dispatchers.Main) {
            while(true) {
                delay(TIEMPO)
                reubicate()
            }
        }
    }

    private fun notificar() {
        val message = "You must be granted the permission of location."
        ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
        ubicacion()
    }

    private fun mensaje(location: Location?) {
        val sharedPreferences = activity?.getSharedPreferences("user_movies", AppCompatActivity.MODE_PRIVATE)
        val user_local = sharedPreferences?.getString("user", "")
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
                val message = "Right now you are in \nLatitude: ${location.latitude} \nLongitude: ${location.longitude}"
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
                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notificationLocation(msj: String){
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Your Location")
            .setContentText(msj)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        with(NotificationManagerCompat.from(requireContext())) {
            notify(2, builder.build())
        }
    }
}