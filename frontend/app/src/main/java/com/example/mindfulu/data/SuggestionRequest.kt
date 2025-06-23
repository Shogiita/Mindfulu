package com.example.mindfulu.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SuggestionRequest(
    val mood: String,
    val reason: String
)