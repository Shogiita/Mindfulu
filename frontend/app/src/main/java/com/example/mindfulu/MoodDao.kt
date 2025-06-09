package com.example.mindfulu

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MoodDao {
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insert(mood: MoodEntity)

    @Query("DELETE FROM mood")
    suspend fun deleteAll()
}