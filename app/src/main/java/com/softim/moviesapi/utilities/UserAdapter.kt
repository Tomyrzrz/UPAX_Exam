package com.softim.moviesapi.utilities

import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.softim.moviesapi.R
import com.softim.moviesapi.data.models.ModelUser
import com.softim.moviesapi.databinding.ItemUserBinding
import com.squareup.picasso.Picasso

class UserAdapter(val users: List<ModelUser>): RecyclerView.Adapter<UserAdapter.ViewHolderUser>() {
    inner class ViewHolderUser(itemView: View): RecyclerView.ViewHolder(itemView){
        private val binding = ItemUserBinding.bind(itemView)
        fun bind(user: ModelUser){
            binding.ivAvatar.setImageURI(user.imagen.toUri())
            Picasso.get().load(user.imagen).into(binding.ivAvatar)
            binding.tvName.setText(user.nombre)
            binding.tvCorreo.setText(user.correo)
            binding.tvDomicilio.setText(user.direccion)
            binding.tvTelefono.setText(user.telefono)
            binding.tvUid.setText(user.codigo.toString())
        }
    }


    override fun getItemCount(): Int {
        return users.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderUser {
        return ViewHolderUser(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderUser, position: Int) {
        val item = users[position]
        holder.bind(item)
    }
}