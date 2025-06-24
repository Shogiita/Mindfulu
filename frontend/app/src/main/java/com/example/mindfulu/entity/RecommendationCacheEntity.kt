package com.example.mindfulu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendation_cache")
data class RecommendationCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // ID otomatis untuk setiap entri cache
    val userEmail: String,
    val date: Long, // Tanggal rekomendasi (timestamp)
    val suggestionsJson: String // Seluruh objek SuggestionResponse dalam bentuk JSON string
)