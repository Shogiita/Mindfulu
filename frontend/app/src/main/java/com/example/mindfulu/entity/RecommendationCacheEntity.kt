package com.example.mindfulu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendation_cache")
data class RecommendationCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val date: Long,
    val suggestionsJson: String
)