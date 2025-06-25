package com.example.mindfulu.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mindfulu.App
import com.example.mindfulu.data.SuggestionResponse
import com.example.mindfulu.entity.RecommendationCacheEntity
import com.example.mindfulu.repository.RecommendationCacheRepository
import com.example.mindfulu.repository.SuggestionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

// Change to AndroidViewModel to get application context
class SuggestionViewModel(application: Application) : AndroidViewModel(application) {
    private val suggestionRepository = SuggestionRepository()
    // [NEW] Initialize repository for cache
    private val cacheRepository = RecommendationCacheRepository(application)
    // [NEW] Get Moshi instance from App class
    private val moshi = App.moshi

    private val _suggestions = MutableLiveData<SuggestionResponse?>()
    val suggestions: MutableLiveData<SuggestionResponse?> get() = _suggestions

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getSuggestions(mood: String, reason: String, email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = suggestionRepository.getSuggestions(mood, reason)
                result.fold(
                    onSuccess = { suggestionResponse ->
                        _suggestions.postValue(suggestionResponse)
                        cacheSuggestion(email, suggestionResponse)
                        Log.d("SuggestionViewModel", "Suggestions fetched and cached successfully.")
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

    fun loadSuggestionsForToday(email: String, mood: String, reason: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val cached = cacheRepository.getRecommendationForToday(email)
                if (cached != null) {
                    val suggestionResponse = moshi.adapter(SuggestionResponse::class.java).fromJson(cached.suggestionsJson)
                    if (suggestionResponse != null) {
                        _suggestions.postValue(suggestionResponse)
                        Log.d("SuggestionViewModel", "Suggestions loaded from cache.")
                    } else {
                        getSuggestions(mood, reason, email)
                    }
                } else {
                    getSuggestions(mood, reason, email)
                }
            } catch (e: Exception) {
                _error.postValue("Error loading suggestions: ${e.message}")
                Log.e("SuggestionViewModel", "Error in loadSuggestionsForToday: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun cacheSuggestion(email: String, response: SuggestionResponse) {
        val jsonString = moshi.adapter(SuggestionResponse::class.java).toJson(response)
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val cacheEntity = RecommendationCacheEntity(
            userEmail = email,
            date = startOfDay,
            suggestionsJson = jsonString
        )
        cacheRepository.saveRecommendation(cacheEntity)
    }
}
