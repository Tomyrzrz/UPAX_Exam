package com.softim.moviesapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.softim.moviesapi.fragments.FragmentMovies
import com.softim.moviesapi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fcv_principal, FragmentMovies::class.java, null)
            .commit()

    }


}