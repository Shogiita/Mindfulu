package com.example.mindfulu.response

import com.example.mindfulu.MoodData

data class MoodResponse(
    val message: String,
    val mood: MoodData
)
