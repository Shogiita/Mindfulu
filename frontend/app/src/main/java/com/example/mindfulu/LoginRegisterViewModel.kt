package com.example.mindfulu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a222117007_m7.App
// TAMBAHKAN SEMUA IMPORT DARI PAKET 'data' DI SINI
import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.ErrorResponse
import com.example.mindfulu.data.LoginRequest
import com.example.mindfulu.data.RegisterRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginRegisterViewModel : ViewModel() {

    private val _registerResult = MutableLiveData<AuthResponse>()
    val registerResult: LiveData<AuthResponse> get() = _registerResult

    private val _loginResult = MutableLiveData<AuthResponse>()
    val loginResult: LiveData<AuthResponse> get() = _loginResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val apiService = App.retrofitService

    fun login(username: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = LoginRequest(username, password) // Baris ini butuh import
                val response = apiService.login(request)

                if (response.isSuccessful) {
                    _loginResult.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = App.moshi.adapter(ErrorResponse::class.java).fromJson(errorBody ?: "")
                        errorResponse?.message ?: "Unknown login error"
                    } catch (e: Exception) {
                        "Invalid username or password"
                    }
                    _error.postValue(errorMessage)
                }
            } catch (e: HttpException) {
                _error.postValue("A network error occurred: ${e.message()}")
            } catch (e: IOException) {
                _error.postValue("Could not connect to the server. Please check your network.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun register(username: String, name: String, email: String, password: String, cpassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = RegisterRequest(username, name, email, password, cpassword) // Baris ini juga butuh import
                val response = apiService.register(request)

                if (response.isSuccessful) {
                    _registerResult.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = App.moshi.adapter(ErrorResponse::class.java).fromJson(errorBody ?: "")
                        errorResponse?.message ?: "Registration failed"
                    } catch (e: Exception) {
                        "An unknown error occurred"
                    }
                    _error.postValue(errorMessage)
                }
            } catch (e: HttpException) {
                _error.postValue("A network error occurred: ${e.message()}")
            } catch (e: IOException) {
                _error.postValue("Could not connect to the server. Please check your network.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}