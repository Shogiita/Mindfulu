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

        return recommendationDao.getRecommendationForDate(email, startOfDay)
    }

    suspend fun cleanOldRecommendations(email: String, daysAgo: Int) {
        val threshold = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }.timeInMillis
        recommendationDao.deleteOldRecommendations(email, threshold)
    }
}