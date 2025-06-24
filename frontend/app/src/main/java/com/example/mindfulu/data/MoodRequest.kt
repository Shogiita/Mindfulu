package com.example.mindfulu.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MoodRequest (
    val mood: String,
    val reason: String,
    val email:String
)