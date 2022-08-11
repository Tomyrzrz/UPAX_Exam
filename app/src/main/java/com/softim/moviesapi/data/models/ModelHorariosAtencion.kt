package com.softim.moviesapi.data.models

import com.google.gson.annotations.SerializedName

data class ModelHorariosAtencion(
    private var  horariosAtencion: List<HorariosAtencion> = emptyList()
)

data class HorariosAtencion (
    @SerializedName("codeDia") private var codeDia : String = "",
    @SerializedName("idTipoHora") private var idTipoHora: Int = 0,
    @SerializedName("tipoHora") private var tipoHora :String = "",
    @SerializedName("hora") private var hora : String =""
        )
