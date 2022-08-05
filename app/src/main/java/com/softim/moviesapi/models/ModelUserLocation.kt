package com.softim.moviesapi.models

import com.google.firebase.Timestamp

data class ModelUserLocation(var user : String = "",
                            var latitude: Double = 0.0,
                            var longitude: Double = 0.0,
                             var time: Timestamp = Timestamp.now()
)
