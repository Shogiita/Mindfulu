package com.example.mindfulu.repository

import com.example.mindfulu.App
import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.UserResponse
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk menangani otentikasi (login dan register) menggunakan Firestore.
 *
 * @property db Instance dari FirebaseFirestore yang disuntikkan (injected) untuk mempermudah testing.
 */
class AuthRepository(private val db: FirebaseFirestore) {
    // webService tidak lagi digunakan di sini, tapi kita biarkan jika ada rencana pengembangan.
    private val webService = App.retrofitService

    /**
     * Melakukan proses login dengan memverifikasi username dan password di Firestore.
     * @param username Username pengguna.
     * @param password Password pengguna (seharusnya sudah di-hash).
     * @return Result yang berisi AuthResponse jika berhasil, atau Exception jika gagal.
     */
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

    /**
     * Melakukan proses registrasi dengan menyimpan data pengguna baru ke Firestore.
     * Memeriksa apakah username atau email sudah ada sebelum membuat akun baru.
     * @return Result yang berisi AuthResponse jika berhasil, atau Exception jika gagal.
     */
    suspend fun registerWithFirestore(
        username: String,
        name: String,
        email: String,
        password: String,
        cpassword: String
    ): Result<AuthResponse> {
        return try {
            // Cek apakah username sudah ada
            val existingUsername = db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (!existingUsername.isEmpty) {
                return Result.failure(Exception("Username already exists."))
            }

            // Cek apakah email sudah ada
            val existingEmail = db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (!existingEmail.isEmpty) {
                return Result.failure(Exception("Email already exists."))
            }

            // Buat data pengguna baru
            val newUser = hashMapOf(
                "username" to username,
                "name" to name,
                "email" to email,
                "password" to password // Seharusnya password yang sudah di-hash
            )

            // Tambahkan pengguna baru ke koleksi 'users'
            db.collection("users").add(newUser).await()

            val userResponse = UserResponse(
                id = 0, // ID bisa di-generate secara berbeda jika perlu
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
