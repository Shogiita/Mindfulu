package com.example.mindfulu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val userEmail: String, // Email pengguna sebagai PK
    val isLoggedIn: Boolean,
    val lastLoginTimestamp: Long // Timestamp login terakhir untuk referensi
)