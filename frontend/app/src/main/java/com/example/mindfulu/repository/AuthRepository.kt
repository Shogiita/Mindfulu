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
    private val db = FirebaseFirestore.getInstance() // Firestore instance tetap digunakan

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
                val storedPassword = userDocument.getString("password") // Ini akan mengambil password yang sudah di-hash
                val storedEmail = userDocument.getString("email")
                val storedName = userDocument.getString("name")
                val userId = userDocument.id

                // [PENTING] Perbandingan sekarang adalah antara hashed password yang diinput dan hashed password yang disimpan
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
        password: String, // Ini akan menerima password yang sudah di-hash dari frontend
        cpassword: String // Ini juga akan menerima password yang sudah di-hash
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
                "password" to password // Sekarang ini menyimpan password yang sudah di-hash
            )

            db.collection("users").add(newUser).await()

            val userResponse = UserResponse(
                id = 0, // ID ini perlu di-handle dengan benar, mungkin dari docRef.id
                username = username,
                name = name,
                email = email
            )
            Result.success(AuthResponse("Registration successful!", userResponse))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // API Login (pastikan ini tetap dikomentari atau dihapus jika tidak digunakan)
    // suspend fun loginWithAPI(username: String, password: String): Result<AuthResponse> { ... }

    // API Register (pastikan ini tetap dikomentari atau dihapus jika tidak digunakan)
    // suspend fun registerWithAPI(username: String, name: String, email: String, password: String, cpassword: String): Result<AuthResponse> { ... }
}