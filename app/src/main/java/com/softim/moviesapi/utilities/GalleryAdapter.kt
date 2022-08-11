package com.softim.moviesapi.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.softim.moviesapi.R
import com.softim.moviesapi.data.models.ModelUserImages
import com.softim.moviesapi.data.models.Model_business
import com.softim.moviesapi.databinding.ItemBusineBinding
import com.softim.moviesapi.databinding.ItemPhotoBinding
import com.squareup.picasso.Picasso

class GalleryAdapter(val images: List<ModelUserImages>)
    : RecyclerView.Adapter<GalleryAdapter.businessViewHolder>() {


    class businessViewHolder(view: View):RecyclerView.ViewHolder(view) {

        private val binding = ItemPhotoBinding.bind(view)
        fun bind(image: ModelUserImages){
            Picasso.get().load(image.imagen).into(binding.imgGalleryItem)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): businessViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return businessViewHolder(layoutInflater.inflate(R.layout.item_photo, parent, false))
    }
    override fun getItemCount(): Int = images.size
    override fun onBindViewHolder(holder: businessViewHolder, position: Int) {
        val item = images[position]
        holder.bind(item)
    }


}