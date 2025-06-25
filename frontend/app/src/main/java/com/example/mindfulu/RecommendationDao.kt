// frontend/app/src/main/java/com/example/mindfulu/RecommendationDao.kt
package com.example.mindfulu

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mindfulu.entity.RecommendationCacheEntity

@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: RecommendationCacheEntity)

    @Query("SELECT * FROM recommendation_cache WHERE userEmail = :email AND date = :date LIMIT 1")
    suspend fun getRecommendationForDate(email: String, date: Long): RecommendationCacheEntity?

    @Query("DELETE FROM recommendation_cache WHERE userEmail = :email AND date < :thresholdDate")
    suspend fun deleteOldRecommendations(email: String, thresholdDate: Long)

    @Query("DELETE FROM recommendation_cache")
    suspend fun deleteAllRecommendations()
}