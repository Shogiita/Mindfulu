package com.example.mindfulu.data

import com.squareup.moshi.JsonClass

// Untuk body request saat login
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)

// Untuk body request saat register
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val name: String,
    val email: String,
    val password: String,
    val cpassword: String
)