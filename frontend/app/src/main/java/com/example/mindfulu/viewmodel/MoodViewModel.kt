package com.example.mindfulu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfulu.MoodData
import com.example.mindfulu.repository.MoodRepository
import com.example.mindfulu.data.MoodResponse
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

    fun postMood(mood: String, reason: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = moodRepository.postMood(mood, reason)
            result.fold(
                onSuccess = { moodResponse ->
                    _moodResult.postValue(moodResponse)
                },
                onFailure = { exception ->
                    _error.postValue(exception.message ?: "Failed to post mood")
                }
            )
            _isLoading.postValue(false)
        }
    }

    fun getAllMoods() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = moodRepository.getAllMoods()
            result.fold(
                onSuccess = { moods ->
                    _moodHistory.postValue(moods)
                },
                onFailure = { exception ->
                    _error.postValue(exception.message ?: "Failed to get mood history")
                }
            )
            _isLoading.postValue(false)
        }
    }
}