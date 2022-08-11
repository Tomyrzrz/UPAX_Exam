package com.softim.moviesapi.data.models

data class ModelUser(var nombre : String = "",
                     var telefono: String = "",
                     var correo: String = "",
                     var direccion: String = "",
                     var imagen: ByteArray
)
