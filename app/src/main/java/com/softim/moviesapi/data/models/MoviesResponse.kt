package com.softim.moviesapi.data.models

import com.google.gson.annotations.SerializedName

data class MoviesResponse(
    @SerializedName("page") var status: Int,
    @SerializedName("results") var movies: List<Model_movie>
)