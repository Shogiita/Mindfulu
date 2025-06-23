package com.example.mindfulu.repository

import com.example.mindfulu.App
import com.example.mindfulu.MoodData
import com.example.mindfulu.MoodRequest
import com.example.mindfulu.data.MoodResponse

class MoodRepository {
    private val webService = App.retrofitService

    suspend fun postMood(mood: String, reason: String): Result<MoodResponse> {
        return try {
            val request = MoodRequest(mood, reason)
            val response = webService.postMood(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad request: Mood and reason are required"
                    500 -> "Server error occurred"
                    404 -> "Endpoint not found"
                    else -> "HTTP ${response.code()}: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllMoods(): Result<List<MoodData>> {
        return try {
            val response = webService.getMood()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = when (response.code()) {
                    500 -> "Server error occurred"
                    404 -> "Endpoint not found"
                    else -> "HTTP ${response.code()}: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}