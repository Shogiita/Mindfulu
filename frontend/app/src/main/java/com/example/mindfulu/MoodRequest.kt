package com.example.mindfulu

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MoodRequest(
    val mood: String,
    val reason: String,
    val email: String
)
