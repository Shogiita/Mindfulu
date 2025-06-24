// frontend/app/src/main/java/com/example/mindfulu/repository/SessionRepository.kt
package com.example.mindfulu.repository

import android.content.Context
import com.example.mindfulu.AppDatabase
import com.example.mindfulu.entity.UserSessionEntity

class SessionRepository(context: Context) {
    private val userSessionDao = AppDatabase.getInstance(context).userSessionDao()

    suspend fun saveUserSession(session: UserSessionEntity) {
        userSessionDao.insertUserSession(session)
    }

    suspend fun getActiveUserSession(): UserSessionEntity? {
        return userSessionDao.getActiveSession()
    }

    suspend fun deleteUserSession(email: String) {
        userSessionDao.deleteUserSession(email)
    }
}