package com.softim.moviesapi.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.softim.moviesapi.R
import com.softim.moviesapi.data.models.ExamUPAXDto
import com.softim.moviesapi.data.models.ModelDirecciones
import com.softim.moviesapi.data.models.Model_business
import com.softim.moviesapi.data.network.APIservice
import com.softim.moviesapi.databinding.FragmentHomeBinding
import com.softim.moviesapi.utilities.BusinessAdapter
import com.softim.moviesapi.utilities.BusinessLocalBD
import com.softim.moviesapi.utilities.ExceptionDialogFragment
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() , BusinessAdapter.onItemClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: BusinessAdapter
    private val moviesImages = mutableListOf<Model_business>()

    private var bd = Firebase.firestore
    private lateinit var ref: DocumentReference
    private val CHANNEL_ID = "MOVIESAPI"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        createNotificationChannel()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        searchByName()
    }

    override fun onButtonImage(ubica: Model_business) {
        super.onButtonImage(ubica)
        var longitud= ""
        var latitud = ""
        val nombre = ubica.nombre
        for (bus in ubica.direcciones){
            longitud = bus.longitud
            latitud = bus.latitud
        }
        val bundle = Bundle()
        bundle.putDouble("latitud", latitud.toDouble())
        bundle.putDouble("longitud", longitud.toDouble())
        bundle.putString("nombre", nombre)
        Navigation.findNavController(activity!!.parent, R.id.nav_host_fragment_home).navigate(R.id.nav_slideshow, bundle);
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView() {
        adapter = BusinessAdapter(moviesImages, this)
        binding.rvMovies.layoutManager = LinearLayoutManager(requireContext())
            .apply {
                binding.rvMovies.layoutManager = this
            }
        binding.rvMovies.adapter = adapter
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://2mg0a7e51k.execute-api.us-east-1.amazonaws.com/v1/miruta/")
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

    private fun searchByName(){
        if (isNetworkAvailable(requireContext())) {
            CoroutineScope(Dispatchers.IO).launch {
                val call = getRetrofit().create(APIservice::class.java).getBusiness(ExamUPAXDto(149010))
                val list_business = call.body()

                activity?.runOnUiThread {
                    if (call.isSuccessful) {
                        val admin = BusinessLocalBD(requireContext(), "business", null, 1)
                        val bd = admin.writableDatabase
                        val directions = list_business?.bussine?: emptyList()
                        for (direc in directions){
                            val registro = ContentValues()
                            registro.put("imagen", direc.urlImagen)
                            registro.put("nombre", direc.nombre)
                            for (dir in direc.direcciones){
                                registro.put("calle", dir.calle)
                                registro.put("numeroExterior", dir.numeroExterior)
                                registro.put("numeroInterior", dir.numeroInterior)
                                registro.put("codigoPostal", dir.codigoPostal)
                                registro.put("colonia", dir.colonia)
                                registro.put("nombrePais", dir.nombrePais)
                                registro.put("nombreEstado", dir.nombreEstado)
                                registro.put("nombreMunicipio", dir.nombreMunicipio)
                                registro.put("latitud", dir.latitud)
                                registro.put("longitud", dir.longitud)
                            }

                            bd.insert("business", null, registro)
                        }
                        bd.close()
                        moviesImages.clear()
                        moviesImages.addAll(directions)
                        adapter.notifyDataSetChanged()
                    } else {
                        val message = "We have an error"
                        ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
                    }
                }
            }
        }else{
            val admin = BusinessLocalBD(requireContext(),"movies_local", null, 1)
            val bd = admin.writableDatabase
            val fila = bd.rawQuery("select * from business", null)
            if (fila.moveToFirst()) {
                do {
                    val imagen: String = fila.getString(1)
                    val nombre: String = fila.getString(2)
                    val direcciones : MutableList<ModelDirecciones> = mutableListOf()
                    direcciones.add(ModelDirecciones(fila.getString(3), fila.getString(4) , fila.getString(5),
                        fila.getString(6), fila.getString(7), fila.getString(8), fila.getString(9), fila.getString(10),
                        fila.getString(11), fila.getString(12)))
                    val bus = Model_business(imagen, nombre, direcciones)
                    moviesImages.add(bus)
                    adapter.notifyDataSetChanged()
                } while (fila.moveToNext())
            }else{
                val message = "No Network. Connect to Internet"
                ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
            }
            bd.close()
        }

    }


    private fun notificar() {
        val message = "You must be granted the permission of location."
        ExceptionDialogFragment(message).show(parentFragmentManager, ExceptionDialogFragment.TAG)
    }

    /*private fun mensaje(location: Location?) {
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
    }*/

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
            .setContentTitle("Your Exception is")
            .setContentText(msj)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        with(NotificationManagerCompat.from(requireContext())) {
            notify(2, builder.build())
        }
    }
}