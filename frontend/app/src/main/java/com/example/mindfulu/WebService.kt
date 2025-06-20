package com.example.mindfulu

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface  WebService {
    @POST("mood")
    suspend fun postMood(@Body request: MoodRequest): MoodResponse

    @GET("mood")
    suspend fun getMood(): List<MoodData>
}