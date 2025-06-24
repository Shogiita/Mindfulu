package com.example.mindfulu.repository

import com.example.mindfulu.MoodData
import com.example.mindfulu.MoodRequest
import com.example.mindfulu.WebService
import com.example.mindfulu.data.MoodResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
class MoodRepositoryTest {

    private lateinit var moodRepository: MoodRepository

    @Mock
    private lateinit var mockWebService: WebService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        moodRepository = MoodRepository()
        // Using reflection to inject the mock WebService as it's a companion object property
        val webServiceField = MoodRepository::class.java.getDeclaredField("webService")
        webServiceField.isAccessible = true
        webServiceField.set(moodRepository, mockWebService)
    }

    @Test
    fun `postMood returns success for successful API call`() = runTest {
        val mood = "Happy"
        val reason = "Finished project"
        val email = "test@example.com"
        val expectedMoodData = MoodData(mood, reason, System.currentTimeMillis(), "id123", email)
        val apiResponse = MoodResponse("Mood recorded successfully", expectedMoodData)
        `when`(mockWebService.postMood(MoodRequest(mood, reason, email)))
            .thenReturn(Response.success(apiResponse))

        val result = moodRepository.postMood(mood, reason, email)

        assertTrue(result.isSuccess)
        assertEquals(apiResponse, result.getOrNull())
    }

    @Test
    fun `postMood returns failure for API error`() = runTest {
        val mood = "Sad"
        val reason = "Bad day"
        val email = "test@example.com"
        val errorBody = "{\"message\":\"Internal Server Error\"}".toResponseBody("application/json".toMediaTypeOrNull())
        `when`(mockWebService.postMood(MoodRequest(mood, reason, email)))
            .thenReturn(Response.error(500, errorBody))

        val result = moodRepository.postMood(mood, reason, email)

        assertTrue(result.isFailure)
        assertEquals("Error 500: Server Error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `postMood returns failure for network error`() = runTest {
        val mood = "Happy"
        val reason = "Finished project"
        val email = "test@example.com"
        `when`(mockWebService.postMood(MoodRequest(mood, reason, email)))
            .thenThrow(IOException("Network error"))

        val result = moodRepository.postMood(mood, reason, email)

        assertTrue(result.isFailure)
        assertEquals("Network error. Please check your connection.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getAllMoods returns success for successful API call`() = runTest {
        val email = "test@example.com"
        val expectedMoods = listOf(
            MoodData("Happy", "Good day", 1L, "id1", email),
            MoodData("Sad", "Bad day", 2L, "id2", email)
        )
        `when`(mockWebService.getMoodHistory(email))
            .thenReturn(Response.success(expectedMoods))

        val result = moodRepository.getAllMoods(email)

        assertTrue(result.isSuccess)
        assertEquals(expectedMoods, result.getOrNull())
    }

    @Test
    fun `getAllMoods returns failure for API error`() = runTest {
        val email = "test@example.com"
        val errorBody = "{\"message\":\"Unauthorized\"}".toResponseBody("application/json".toMediaTypeOrNull())
        `when`(mockWebService.getMoodHistory(email))
            .thenReturn(Response.error(401, errorBody))

        val result = moodRepository.getAllMoods(email)

        assertTrue(result.isFailure)
        assertEquals("Error 401: Unauthorized", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getAllMoods returns failure for network error`() = runTest {
        val email = "test@example.com"
        `when`(mockWebService.getMoodHistory(email))
            .thenThrow(IOException("No internet"))

        val result = moodRepository.getAllMoods(email)

        assertTrue(result.isFailure)
        assertEquals("Network error. Please check your connection.", result.exceptionOrNull()?.message)
    }
}