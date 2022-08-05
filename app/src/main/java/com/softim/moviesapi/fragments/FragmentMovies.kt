package com.softim.moviesapi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.softim.moviesapi.R
import com.softim.moviesapi.databinding.FragmentMoviesBinding
import com.softim.moviesapi.models.Model_movie
import com.softim.moviesapi.utilities.APIservice
import com.softim.moviesapi.utilities.MoviesAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class FragmentMovies : Fragment(), AdapterView.OnItemSelectedListener, MoviesAdapter.onItemClickListener {

    private lateinit var binding: FragmentMoviesBinding
    private lateinit var adapter: MoviesAdapter
    private val moviesImages = mutableListOf<Model_movie>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    /*private fun initRecyclerView() {
        adapter = MoviesAdapter(moviesImages, this)
        binding.rvMovies.layoutManager = LinearLayoutManager(requireContext())
            .apply {
                binding.rvMovies.layoutManager = this
            }
        binding.rvMovies.adapter = adapter
    }*/


    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/movie/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private fun searchByName(query:String){
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(APIservice::class.java).getMoviesByBreeds("$query?api_key=e8a82c4e3f0e112131ea103fade4eb93")
            val list_movies = call.body()

            activity?.runOnUiThread {
                if (call.isSuccessful) {
                    val pelis = list_movies?.movies ?: emptyList()
                    moviesImages.clear()
                    moviesImages.addAll(pelis)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "We have an error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val texto = p0?.selectedItem.toString()
        searchByName(texto.lowercase(Locale.ROOT))
    }

    override fun onButtonImage(item: View, dog: String) {
        super.onButtonImage(item, dog)
        val args = Bundle()
        args.putString("msj", dog)
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.fcv_principal, FragmentUnitMovie::class.java, args)
            .addToBackStack(FragmentMovies::class.java.name)
            .commit()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Toast.makeText(requireContext(), "Select Breed of Dog.", Toast.LENGTH_LONG).show()
    }

}