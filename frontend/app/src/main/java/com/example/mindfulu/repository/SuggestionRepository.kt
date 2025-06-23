package com.example.mindfulu.repository

import com.example.mindfulu.App
import com.example.mindfulu.data.SuggestionRequest
import com.example.mindfulu.data.SuggestionResponse

class SuggestionRepository {
    private val webService = App.retrofitService

    suspend fun getSuggestions(mood: String, reason: String): Result<SuggestionResponse> {
        return try {
            val request = SuggestionRequest(mood, reason)
            val response = webService.getSuggestions(request)

            if (response.isSuccessful) {
                response.body()?.let { suggestionResponse ->
                    Result.success(suggestionResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get suggestions: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}