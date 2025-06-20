package com.example.mindfulu.data

import com.squareup.moshi.JsonClass

// Untuk response saat login atau register berhasil
@JsonClass(generateAdapter = true)
data class AuthResponse(
    val message: String,
    val user: UserResponse? // User bisa null jika ada error
)

// Untuk data user di dalam response
@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: Int,
    val username: String,
    val name: String,
    val email: String
)

// Untuk menampung pesan error dari backend
@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val message: String
)