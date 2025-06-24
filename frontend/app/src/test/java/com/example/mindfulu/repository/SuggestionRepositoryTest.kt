package com.example.mindfulu.repository

import com.example.mindfulu.WebService
import com.example.mindfulu.data.ActivitySuggestion
import com.example.mindfulu.data.MusicSuggestion
import com.example.mindfulu.data.SuggestionRequest
import com.example.mindfulu.data.SuggestionResponse
import com.example.mindfulu.data.Suggestions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
class SuggestionRepositoryTest {

    private lateinit var suggestionRepository: SuggestionRepository

    @Mock
    private lateinit var mockWebService: WebService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        suggestionRepository = SuggestionRepository()
        // Using reflection to inject the mock WebService
        val webServiceField = SuggestionRepository::class.java.getDeclaredField("webService")
        webServiceField.isAccessible = true
        webServiceField.set(suggestionRepository, mockWebService)
    }

    @Test
    fun `getSuggestions returns success for successful API call`() = runTest {
        val mood = "Happy"
        val reason = "Feeling great"
        val musicSuggestion = MusicSuggestion("Song Title", "Artist", "Reason", "link")
        val activitySuggestion = ActivitySuggestion("Activity", "Description")
        val suggestions = Suggestions(musicSuggestion, listOf(activitySuggestion))
        val apiResponse = SuggestionResponse("Success", suggestions)

        `when`(mockWebService.getSuggestions(SuggestionRequest(mood, reason)))
            .thenReturn(Response.success(apiResponse))

        val result = suggestionRepository.getSuggestions(mood, reason)

        assertTrue(result.isSuccess)
        assertEquals(apiResponse, result.getOrNull())
    }

    @Test
    fun `getSuggestions returns failure for API error`() = runTest {
        val mood = "Sad"
        val reason = "Long day"
        val errorBody = "{\"message\":\"API Rate Limit Exceeded\"}".toResponseBody("application/json".toMediaTypeOrNull())

        `when`(mockWebService.getSuggestions(SuggestionRequest(mood, reason)))
            .thenReturn(Response.error(429, errorBody))

        val result = suggestionRepository.getSuggestions(mood, reason)

        assertTrue(result.isFailure)
        assertEquals("Error 429: Too Many Requests", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getSuggestions returns failure for network error`() = runTest {
        val mood = "Happy"
        val reason = "Feeling great"

        `when`(mockWebService.getSuggestions(SuggestionRequest(mood, reason)))
            .thenThrow(IOException("No network connection"))

        val result = suggestionRepository.getSuggestions(mood, reason)

        assertTrue(result.isFailure)
        assertEquals("Network error. Please check your connection.", result.exceptionOrNull()?.message)
    }
}