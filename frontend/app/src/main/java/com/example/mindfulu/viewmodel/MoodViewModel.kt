package com.example.mindfulu.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulu.MoodData
import com.example.mindfulu.data.MoodResponse
import com.example.mindfulu.repository.MoodRepository
import kotlinx.coroutines.launch

class MoodViewModel : ViewModel() {
    private val moodRepository = MoodRepository()

    private val _moodResult = MutableLiveData<MoodResponse>()
    val moodResult: LiveData<MoodResponse> get() = _moodResult

    private val _moodHistory = MutableLiveData<List<MoodData>>()
    val moodHistory: LiveData<List<MoodData>> get() = _moodHistory

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun postMood(mood: String, reason: String, email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = moodRepository.postMood(mood, reason, email)
                result.fold(
                    onSuccess = { moodResponse ->
                        _moodResult.postValue(moodResponse)
                        Log.d("MoodViewModel", "Mood posted successfully: ${moodResponse.message}")
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("ConnectException") == true ->
                                "Cannot connect to server. Please check your internet connection and server status."
                            exception.message?.contains("SocketTimeoutException") == true ->
                                "Connection timeout. Please try again."
                            else -> exception.message ?: "Failed to post mood"
                        }
                        _error.postValue(errorMessage)
                        Log.e("MoodViewModel", "Error posting mood: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.postValue("Unexpected error: ${e.message}")
                Log.e("MoodViewModel", "Unexpected error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getAllMoods(email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = moodRepository.getAllMoods(email)
                result.fold(
                    onSuccess = { moods ->
                        _moodHistory.postValue(moods)
                        Log.d("MoodViewModel", "Fetched ${moods.size} moods")
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("ConnectException") == true ->
                                "Cannot connect to server. Please check your internet connection and server status."
                            exception.message?.contains("SocketTimeoutException") == true ->
                                "Connection timeout. Please try again."
                            else -> exception.message ?: "Failed to get mood history"
                        }
                        _error.postValue(errorMessage)
                        Log.e("MoodViewModel", "Error fetching moods: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.postValue("Unexpected error: ${e.message}")
                Log.e("MoodViewModel", "Unexpected error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}