package com.softim.moviesapi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.softim.moviesapi.databinding.FragmentUnitMovieBinding
import com.squareup.picasso.Picasso

class FragmentUnitMovie: Fragment() {

    private lateinit var binding: FragmentUnitMovieBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUnitMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        val movie = args!!.getString("msj", "Not Found")

        Picasso.get().load(movie).into(binding.imgDog)

    }
}