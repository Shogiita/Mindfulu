package com.example.mindfulu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val userEmail: String,
    val isLoggedIn: Boolean,
    val lastLoginTimestamp: Long
)