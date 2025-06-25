package com.example.mindfulu.repository

import com.example.mindfulu.App
import com.example.mindfulu.MoodData
import com.example.mindfulu.MoodRequest
import com.example.mindfulu.data.MoodResponse
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MoodRepository {
    private val webService = App.retrofitService

    suspend fun postMood(mood: String, reason: String, email: String): Result<MoodResponse> {
        return try {
            val request = MoodRequest(mood, reason, email)
            val response: Response<MoodResponse> = webService.postMood(request)

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

    // [DIUBAH] Fungsi sekarang menerima email untuk dikirim sebagai query parameter
    suspend fun getAllMoods(email: String): Result<List<MoodData>> {
        return try {
            val response: Response<List<MoodData>> = webService.getMoodHistory(email)

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
