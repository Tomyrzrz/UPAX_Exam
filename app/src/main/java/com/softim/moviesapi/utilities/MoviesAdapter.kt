package com.softim.moviesapi.utilities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.softim.moviesapi.R
import com.softim.moviesapi.databinding.ItemMovieBinding
import com.softim.moviesapi.data.models.Model_movie
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject

class MoviesAdapter(val images: List<Model_movie>)
    : RecyclerView.Adapter<MoviesAdapter.DogViewHolder>() {

    interface onItemClickListener{
        fun onButtonImage(item: View, dog: String){}
    }

    class DogViewHolder(view: View):RecyclerView.ViewHolder(view) {

        private val binding = ItemMovieBinding.bind(view)
        var imagen = ""

        fun bind(image: Model_movie){
            val url_imagen = "https://image.tmdb.org/t/p/w500/${image.poster_path}"
            Picasso.get().load(url_imagen).into(binding.ivMovie)
            binding.txtTitleMovie.setText(image.title)
            binding.txtDescriptionMovie.setText(image.overview)
            binding.txtRatingMovie.setText("Rating: ${image.vote_average}")
            this.imagen = image.poster_path
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DogViewHolder(layoutInflater.inflate(R.layout.item_movie, parent, false))
    }
    override fun getItemCount(): Int = images.size
    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val item = images[position]
        holder.bind(item)
    }


}