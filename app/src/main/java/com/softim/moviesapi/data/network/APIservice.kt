package com.softim.moviesapi.data.network

import com.softim.moviesapi.data.models.ExamUPAXDto
import com.softim.moviesapi.data.models.BusinessResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface APIservice {
    @POST("comercioDetalleExamen")
    suspend fun getBusiness(@Body  dtoExamUPAX: ExamUPAXDto): Response<BusinessResponse>
}