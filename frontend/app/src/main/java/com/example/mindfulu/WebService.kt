package com.example.mindfulu

import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.LoginRequest
import com.example.mindfulu.data.RegisterRequest
import com.example.mindfulu.data.SuggestionRequest
import com.example.mindfulu.data.MoodResponse
import com.example.mindfulu.data.SuggestionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WebService {
    @POST("mood")
    suspend fun postMood(@Body request: MoodRequest): Response<MoodResponse>

    @GET("mood")
    suspend fun getMoodHistory(): Response<List<MoodData>>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("suggestions")
    suspend fun getSuggestions(@Body request: SuggestionRequest): Response<SuggestionResponse>
}
