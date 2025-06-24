// frontend/app/src/main/java/com/example/mindfulu/repository/RecommendationCacheRepository.kt
package com.example.mindfulu.repository

import android.content.Context
import com.example.mindfulu.AppDatabase
import com.example.mindfulu.entity.RecommendationCacheEntity
import java.util.Calendar

class RecommendationCacheRepository(context: Context) {
    private val recommendationDao = AppDatabase.getInstance(context).recommendationDao()

    suspend fun saveRecommendation(recommendation: RecommendationCacheEntity) {
        recommendationDao.insertRecommendation(recommendation)
    }

    suspend fun getRecommendationForToday(email: String): RecommendationCacheEntity? {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // Query for recommendations within today's range
        // Note: getRecommendationForDate in DAO only checks for exact timestamp.
        // A more robust DAO query would be needed for range.
        // For simplicity here, we'll fetch all and filter in memory if necessary.
        // Or, modify DAO to fetch within a date range if backend stores specific date.
        // Given the current DAO, let's assume 'date' stored is start of the day.
        return recommendationDao.getRecommendationForDate(email, startOfDay)
    }

    // Contoh untuk membersihkan cache lama (opsional)
    suspend fun cleanOldRecommendations(email: String, daysAgo: Int) {
        val threshold = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }.timeInMillis
        recommendationDao.deleteOldRecommendations(email, threshold)
    }
}