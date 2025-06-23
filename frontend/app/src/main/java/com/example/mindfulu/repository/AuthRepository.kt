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
    private val db = FirebaseFirestore.getInstance()

    // Firebase Firestore Login
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

    // Firebase Firestore Register
    suspend fun registerWithFirestore(
        username: String,
        name: String,
        email: String,
        password: String,
        cpassword: String
    ): Result<AuthResponse> {
        return try {
            // Check existing username
            val existingUsername = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (!existingUsername.isEmpty) {
                return Result.failure(Exception("Username already exists."))
            }

            // Check existing email
            val existingEmail = db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (!existingEmail.isEmpty) {
                return Result.failure(Exception("Email already exists."))
            }

            // Create new user
            val newUser = hashMapOf(
                "username" to username,
                "name" to name,
                "email" to email,
                "password" to password // TODO: Hash this password!
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

    // API Login (if you want to use your backend API instead)
    suspend fun loginWithAPI(username: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(username, password)
            val response = webService.login(request)

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // API Register (if you want to use your backend API instead)
    suspend fun registerWithAPI(
        username: String,
        name: String,
        email: String,
        password: String,
        cpassword: String
    ): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(username, name, email, password, cpassword)
            val response = webService.register(request)

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}