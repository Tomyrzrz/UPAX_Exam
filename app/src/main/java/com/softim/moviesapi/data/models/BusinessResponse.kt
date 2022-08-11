package com.softim.moviesapi.data.models

import com.google.gson.annotations.SerializedName

data class BusinessResponse(
    @SerializedName("comercio") var bussine: List<Model_business>,
    @SerializedName("horariosAtencion") var horarios: List<ModelHorariosAtencion>
)