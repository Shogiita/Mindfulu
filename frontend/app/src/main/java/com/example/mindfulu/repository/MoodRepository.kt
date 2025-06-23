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
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllMoods(): Result<List<MoodData>> {
        return try {
            val response = webService.getMood()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
