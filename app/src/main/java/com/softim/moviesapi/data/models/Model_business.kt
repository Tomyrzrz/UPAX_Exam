package com.softim.moviesapi.data.models

data class Model_business(var urlImagen: String = "",
                          var nombre: String = "",
                          var direcciones: List<ModelDirecciones> = emptyList()
                       )