package com.softim.moviesapi.ui.home

import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.softim.moviesapi.databinding.FragmentHomeBinding
import com.softim.moviesapi.models.Model_movie
import com.softim.moviesapi.utilities.APIservice
import com.softim.moviesapi.utilities.MoviesAdapter
import com.softim.moviesapi.utilities.MoviesLocalBD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class HomeFragment : Fragment(), AdapterView.OnItemSelectedListener  {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter: MoviesAdapter
    private val moviesImages = mutableListOf<Model_movie>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)*/
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
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
                        Toast.makeText(requireContext(), "We have an error", Toast.LENGTH_LONG)
                            .show()
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
                Toast.makeText(requireContext(), "No Network. Connect to Internet", Toast.LENGTH_SHORT).show()
            }
            bd.close()
        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val texto = p0?.selectedItem.toString()
        searchByName(texto.lowercase(Locale.ROOT))
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Toast.makeText(requireContext(), "Select Breed of Dog.", Toast.LENGTH_LONG).show()
    }
}