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

    suspend fun postMood(mood: String, reason: String): Result<MoodResponse> {
        return try {
            val request = MoodRequest(mood, reason)
            // Memanggil webService dan mendapatkan objek Response secara eksplisit
            val response: Response<MoodResponse> = webService.postMood(request)

            // Memeriksa apakah respons dari server berhasil (kode 2xx)
            if (response.isSuccessful && response.body() != null) {
                // Jika berhasil, kirim body dari respons, yang bertipe MoodResponse
                Result.success(response.body()!!)
            } else {
                // Jika gagal, buat pesan error berdasarkan kode status
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            // Menangani error HTTP spesifik (seperti 404, 500)
            val errorMessage = "HTTP Error ${e.code()}: ${e.message()}"
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            // Menangani error koneksi jaringan (seperti tidak ada internet)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: Exception) {
            // Menangani error lain yang tidak terduga
            Result.failure(e)
        }
    }

    suspend fun getAllMoods(): Result<List<MoodData>> {
        return try {
            // Memanggil webService dan mendapatkan objek Response secara eksplisit
            val response: Response<List<MoodData>> = webService.getMoodHistory()

            // Memeriksa apakah respons dari server berhasil (kode 2xx)
            if (response.isSuccessful && response.body() != null) {
                // Jika berhasil, kirim body dari respons, yang bertipe List<MoodData>
                Result.success(response.body()!!)
            } else {
                // Jika gagal, buat pesan error berdasarkan kode status
                val errorMessage = "Error ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            // Menangani error HTTP spesifik
            val errorMessage = "HTTP Error ${e.code()}: ${e.message()}"
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            // Menangani error koneksi jaringan
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: Exception) {
            // Menangani error lain
            Result.failure(e)
        }
    }
}
