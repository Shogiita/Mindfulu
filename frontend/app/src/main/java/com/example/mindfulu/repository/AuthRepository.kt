package com.example.mindfulu.repository

import com.example.mindfulu.App
import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.LoginRequest
import com.example.mindfulu.data.RegisterRequest
import com.example.mindfulu.data.UserResponse
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val webService = App.retrofitService
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun loginWithFirestore(username: String, password: String): Result<AuthResponse> {
        return try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Result.failure(Exception("Username not found."))
            } else {
                val userDocument = querySnapshot.documents[0]
                val storedPassword = userDocument.getString("password")
                val storedEmail = userDocument.getString("email")
                val storedName = userDocument.getString("name")
                val userId = userDocument.id

               if (storedPassword == password) {
                    val userResponse = UserResponse(
                        id = userId.hashCode(),
                        username = username,
                        name = storedName ?: "",
                        email = storedEmail ?: ""
                    )
                    Result.success(AuthResponse("Login successful!", userResponse))
                } else {
                    Result.failure(Exception("Invalid username or password."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithFirestore(
        username: String,
        name: String,
        email: String,
        password: String,
        cpassword: String
    ): Result<AuthResponse> {
        return try {
            val existingUsername = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (!existingUsername.isEmpty) {
                return Result.failure(Exception("Username already exists."))
            }

            val existingEmail = db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (!existingEmail.isEmpty) {
                return Result.failure(Exception("Email already exists."))
            }

            val newUser = hashMapOf(
                "username" to username,
                "name" to name,
                "email" to email,
                "password" to password
            )

            db.collection("users").add(newUser).await()

            val userResponse = UserResponse(
                id = 0,
                username = username,
                name = name,
                email = email
            )
            Result.success(AuthResponse("Registration successful!", userResponse))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}