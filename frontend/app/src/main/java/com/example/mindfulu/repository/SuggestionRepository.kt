package com.example.mindfulu.repository

import com.example.mindfulu.App
import com.example.mindfulu.data.SuggestionRequest
import com.example.mindfulu.data.SuggestionResponse
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SuggestionRepository {
    private val webService = App.retrofitService

    suspend fun getSuggestions(mood: String, reason: String): Result<SuggestionResponse> {
        return try {
            val request = SuggestionRequest(mood, reason)
            val response: Response<SuggestionResponse> = webService.getSuggestions(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = "HTTP Error ${e.code()}: ${e.message()}"
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}