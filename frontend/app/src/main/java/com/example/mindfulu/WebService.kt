package com.example.mindfulu

// Import data class yang akan digunakan
import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.LoginRequest
import com.example.mindfulu.data.RegisterRequest
import com.example.mindfulu.response.MoodResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WebService {
    // Endpoint yang sudah ada untuk Mood
    @POST("mood")
    suspend fun postMood(@Body request: MoodRequest): MoodResponse

    @GET("mood")
    suspend fun getMood(): List<MoodData>

    // Endpoint untuk Register
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Endpoint untuk Login
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}