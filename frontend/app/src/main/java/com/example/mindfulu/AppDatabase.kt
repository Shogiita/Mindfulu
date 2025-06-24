package com.example.mindfulu

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mindfulu.entity.RecommendationCacheEntity
import com.example.mindfulu.entity.UserSessionEntity

// [DIUBAH] Tambahkan entitas baru ke array entities
@Database(entities = [MoodEntity::class, UserSessionEntity::class, RecommendationCacheEntity::class], version = 2) // [DIUBAH] Tingkatkan versi database menjadi 2
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao
    // [BARU] Deklarasi DAO baru
    abstract fun userSessionDao(): UserSessionDao
    abstract fun recommendationDao(): RecommendationDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindfulu" // Nama database
                )
                    .fallbackToDestructiveMigration() // [PENTING] Gunakan ini saat development untuk mengizinkan perubahan skema database tanpa migrasi
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}