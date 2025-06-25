package com.example.mindfulu.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val username: String,
    val name: String,
    val email: String,
    val password: String,
    val cpassword: String
)