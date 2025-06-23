package com.example.mindfulu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulu.App
import com.example.mindfulu.data.SuggestionRequest
import com.example.mindfulu.data.SuggestionResponse
import kotlinx.coroutines.launch

class SuggestionViewModel : ViewModel() {
    private val _suggestions = MutableLiveData<SuggestionResponse>()
    val suggestions: LiveData<SuggestionResponse> get() = _suggestions

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getSuggestions(mood: String, reason: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = App.retrofitService.getSuggestions(SuggestionRequest(mood, reason))
                if (response.isSuccessful && response.body() != null) {
                    _suggestions.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Network error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
