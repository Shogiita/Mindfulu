package com.example.mindfulu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginRegisterViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _registerResult = MutableLiveData<AuthResponse>()
    val registerResult: LiveData<AuthResponse> get() = _registerResult

    private val _loginResult = MutableLiveData<AuthResponse>()
    val loginResult: LiveData<AuthResponse> get() = _loginResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun login(username: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.loginWithFirestore(username, password)
                result.fold(
                    onSuccess = { authResponse ->
                        _loginResult.postValue(authResponse)
                    },
                    onFailure = { exception ->
                        _error.postValue(exception.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun register(username: String, name: String, email: String, password: String, cpassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.registerWithFirestore(username, name, email, password, cpassword)
                result.fold(
                    onSuccess = { authResponse ->
                        _registerResult.postValue(authResponse)
                    },
                    onFailure = { exception ->
                        _error.postValue(exception.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}