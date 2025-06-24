package com.example.mindfulu.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulu.data.SuggestionResponse
import com.example.mindfulu.repository.SuggestionRepository
import kotlinx.coroutines.launch

class SuggestionViewModel : ViewModel() {
    private val suggestionRepository = SuggestionRepository()

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
                val result = suggestionRepository.getSuggestions(mood, reason)
                result.fold(
                    onSuccess = { suggestionResponse ->
                        _suggestions.postValue(suggestionResponse)
                        Log.d("SuggestionViewModel", "Suggestions fetched successfully.")
                    },
                    onFailure = { exception ->
                        _error.postValue(exception.message ?: "Failed to fetch suggestions")
                        Log.e("SuggestionViewModel", "Error fetching suggestions: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.postValue("Unexpected error: ${e.message}")
                Log.e("SuggestionViewModel", "Unexpected error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
