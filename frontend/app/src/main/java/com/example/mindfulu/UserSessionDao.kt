// frontend/app/src/main/java/com/example/mindfulu/UserSessionDao.kt
package com.example.mindfulu

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mindfulu.entity.UserSessionEntity

@Dao
interface UserSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSession(session: UserSessionEntity)

    @Update
    suspend fun updateUserSession(session: UserSessionEntity)

    @Query("SELECT * FROM user_sessions WHERE userEmail = :email LIMIT 1")
    suspend fun getUserSession(email: String): UserSessionEntity?

    @Query("DELETE FROM user_sessions WHERE userEmail = :email")
    suspend fun deleteUserSession(email: String)

    @Query("SELECT * FROM user_sessions WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveSession(): UserSessionEntity?
}