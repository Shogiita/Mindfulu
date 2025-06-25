package com.example.mindfulu

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MoodDao {
    @Query("DELETE FROM mood")
    suspend fun deleteAll()
}