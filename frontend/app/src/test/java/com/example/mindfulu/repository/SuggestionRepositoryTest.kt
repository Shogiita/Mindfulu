package com.example.mindfulu.repository

import com.example.mindfulu.App
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
import retrofit2.HttpException
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
        // Set the mock WebService to the App companion object for testing
        val field = App.Companion::class.java.getDeclaredField("retrofitService")
        field.isAccessible = true
        field.set(null, mockWebService)

        suggestionRepository = SuggestionRepository()
    }

    @Test
    fun `getSuggestions success returns success result`() = runTest {
        val mood = "Happy"
        val reason = "Finished project"
        val mockMusicSuggestion = MusicSuggestion("Song Title", "Artist", "Reason", "link")
        val mockActivitySuggestion = listOf(ActivitySuggestion("Activity", "Description"))
        val mockSuggestions = Suggestions(mockMusicSuggestion, mockActivitySuggestion)
        val mockResponse = SuggestionResponse("Suggestions fetched", mockSuggestions)

        `when`(mockWebService.getSuggestions(SuggestionRequest(mood, reason)))
            .thenReturn(Response.success(mockResponse))

        val result = suggestionRepository.getSuggestions(mood, reason)

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getSuggestions unsuccessful response returns failure result`() = runTest {
        val mood = "Happy"
        val reason = "Finished project"

        `when`(mockWebService.getSuggestions(SuggestionRequest(mood, reason)))
            .thenReturn(Response.error(400, "{}".toResponseBody("application/json".toMediaTypeOrNull())))

        val result = suggestionRepository.getSuggestions(mood, reason)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error 400") == true)
    }

    @Test
    fun `getSuggestions network error returns failure result`() = runTest {
        val mood = "Happy"
        val reason = "Finished project"

        `when`(mockWebService.getSuggestions(SuggestionRequest(mood, reason)))
            .thenThrow(IOException("Network error"))

        val result = suggestionRepository.getSuggestions(mood, reason)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertEquals("Network error. Please check your connection.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getSuggestions http exception returns failure result`() = runTest {
        val mood = "Happy"
        val reason = "Finished project"
        val httpException = HttpException(Response.error<SuggestionResponse>(500, "{}".toResponseBody("application/json".toMediaTypeOrNull())))

        `when`(mockWebService.getSuggestions(SuggestionRequest(mood, reason)))
            .thenThrow(httpException)

        val result = suggestionRepository.getSuggestions(mood, reason)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertTrue(result.exceptionOrNull()?.message?.contains("HTTP Error 500") == true)
    }
}