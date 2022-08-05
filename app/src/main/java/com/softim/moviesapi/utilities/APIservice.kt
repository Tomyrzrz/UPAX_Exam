package com.softim.moviesapi.utilities

import com.softim.moviesapi.models.MoviesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface APIservice {
    @GET
    suspend fun getMoviesByBreeds(@Url url:String): Response<MoviesResponse>
}