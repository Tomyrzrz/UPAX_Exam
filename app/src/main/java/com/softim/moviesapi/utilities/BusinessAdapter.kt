package com.softim.moviesapi.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.softim.moviesapi.R
import com.softim.moviesapi.data.models.Model_business
import com.softim.moviesapi.databinding.ItemBusineBinding
import com.squareup.picasso.Picasso

class BusinessAdapter(val images: List<Model_business>, val listener: onItemClickListener)
    : RecyclerView.Adapter<BusinessAdapter.businessViewHolder>() {

    interface onItemClickListener{
        fun onButtonImage(ubica: Model_business){}
    }

    class businessViewHolder(view: View):RecyclerView.ViewHolder(view), View.OnClickListener {

        private val binding = ItemBusineBinding.bind(view)
        val btnLocation: Button
        var listener:onItemClickListener ?= null
        var imagen = ""
        var bussine : Model_business ?= null
        init {
            btnLocation = view.findViewById(R.id.btn_Location)
        }
        fun bind(busi: Model_business, listener: onItemClickListener){
            Picasso.get().load(busi.urlImagen).into(binding.ivMovie)
            binding.txtTitleMovie.setText(busi.nombre)
            for ( dir in busi.direcciones){
                binding.txtDescriptionMovie.setText("${dir.calle} NoExt.${dir.numeroExterior} NoInt.${dir.numeroInterior}, Col.${dir.colonia}, CP:${dir.codigoPostal}")
                binding.txtRatingMovie.setText("${dir.nombrePais}, ${dir.nombreEstado}, ${dir.nombreMunicipio}")
            }
            this.listener = listener
            this.imagen = busi.urlImagen
            this.bussine = busi
        }
        override fun onClick(v: View?) {
            if (v?.id == R.id.btn_Location) {
                listener!!.onButtonImage(bussine!!)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): businessViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return businessViewHolder(layoutInflater.inflate(R.layout.item_busine, parent, false))
    }
    override fun getItemCount(): Int = images.size
    override fun onBindViewHolder(holder: businessViewHolder, position: Int) {
        val item = images[position]
        holder.bind(item, listener)
        holder.btnLocation.setOnClickListener {
            listener.onButtonImage(item)
        }
    }


}