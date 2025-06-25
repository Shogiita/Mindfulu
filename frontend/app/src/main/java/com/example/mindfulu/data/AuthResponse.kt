package com.example.mindfulu.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val message: String,
    val user: UserResponse?
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: Int,
    val username: String,
    val name: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val message: String
)