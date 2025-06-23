package com.example.mindfulu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await // For await() on Task
import kotlinx.coroutines.launch
import com.example.mindfulu.data.AuthResponse // Still used for consistent response structure
import com.example.mindfulu.data.UserResponse // Still used for consistent response structure

class LoginRegisterViewModel : ViewModel() {

    private val _registerResult = MutableLiveData<AuthResponse>()
    val registerResult: LiveData<AuthResponse> get() = _registerResult

    private val _loginResult = MutableLiveData<AuthResponse>()
    val loginResult: LiveData<AuthResponse> get() = _loginResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val db = FirebaseFirestore.getInstance()

    fun login(username: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Mencari user berdasarkan username
                val querySnapshot = db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1) // Ambil hanya 1 hasil
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _error.postValue("Username not found.")
                    return@launch
                }

                val userDocument = querySnapshot.documents[0]
                val storedPassword = userDocument.getString("password")
                val storedEmail = userDocument.getString("email")
                val storedName = userDocument.getString("name")
                val userId = userDocument.id // Ambil ID dokumen Firestore sebagai ID pengguna

                // Memverifikasi password
                // SEKALI LAGI: INI SANGAT TIDAK AMAN KARENA MEMBANDINGKAN PLAINTEXT PASSWORD
                // Anda HARUS MENGGUNAKAN HASHING PASSWORD DI SINI!
                if (storedPassword == password) {
                    val userResponse = UserResponse(
                        id = userId.hashCode(), // Gunakan hashcode ID dokumen sebagai ID sementara
                        username = username,
                        name = storedName ?: "",
                        email = storedEmail ?: ""
                    )
                    _loginResult.postValue(AuthResponse("Login successful!", userResponse))
                } else {
                    _error.postValue("Invalid username or password.")
                }

            } catch (e: Exception) {
                _error.postValue("An error occurred during login: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Ubah fungsi register untuk menggunakan Firestore
    fun register(username: String, name: String, email: String, password: String, cpassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Periksa apakah username sudah ada
                val existingUsername = db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .await()

                if (!existingUsername.isEmpty) {
                    _error.postValue("Username already exists. Please choose another.")
                    return@launch
                }

                // Periksa apakah email sudah ada
                val existingEmail = db.collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()

                if (!existingEmail.isEmpty) {
                    _error.postValue("Email already exists. Please choose another.")
                    return@launch
                }

                // Data user yang akan disimpan
                val newUser = hashMapOf(
                    "username" to username,
                    "name" to name,
                    "email" to email,
                    "password" to password // SEKALI LAGI: INI SANGAT TIDAK AMAN
                )

                // Tambahkan dokumen baru ke koleksi "users"
                db.collection("users")
                    .add(newUser)
                    .await() // Menunggu hingga operasi selesai

                val userResponse = UserResponse(
                    id = 0, // ID akan di-generate oleh Firestore, bisa diisi dummy atau ambil dari document.id
                    username = username,
                    name = name,
                    email = email
                )
                _registerResult.postValue(AuthResponse("Registration successful! You can now log in.", userResponse))

            } catch (e: Exception) {
                _error.postValue("An error occurred during registration: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}