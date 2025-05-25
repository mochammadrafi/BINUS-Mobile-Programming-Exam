package com.exam.exam.data.api

import com.exam.exam.data.model.Hospital
import retrofit2.Response
import retrofit2.http.GET

interface HospitalApiService {
    
    @GET("api/id/covid19/hospitals")
    suspend fun getHospitals(): Response<List<Hospital>>
    
    companion object {
        const val BASE_URL = "https://dekontaminasi.com/"
    }
}