package com.example.mindfulu.repository

import com.example.mindfulu.App
import com.example.mindfulu.MoodData
import com.example.mindfulu.MoodRequest
import com.example.mindfulu.WebService
import com.example.mindfulu.data.MoodResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody

@ExperimentalCoroutinesApi
class MoodRepositoryTest {

    private lateinit var moodRepository: MoodRepository

    @Mock
    private lateinit var mockWebService: WebService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Set the mock WebService to the App companion object for testing
        // This is a workaround since WebService is directly accessed via App.retrofitService
        val field = App.Companion::class.java.getDeclaredField("retrofitService")
        field.isAccessible = true
        field.set(null, mockWebService)

        moodRepository = MoodRepository()
    }

    @Test
    fun `postMood success returns success result`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        val mockMoodData = MoodData(mood, reason, System.currentTimeMillis(), "id123", email)
        val mockResponse = MoodResponse("Mood saved successfully", mockMoodData)

        `when`(mockWebService.postMood(MoodRequest(mood, reason, email)))
            .thenReturn(Response.success(mockResponse))

        val result = moodRepository.postMood(mood, reason, email)

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `postMood unsuccessful response returns failure result`() = runTest {
        val mood = "Sad"
        val reason = "Bad day"
        val email = "test@example.com"

        `when`(mockWebService.postMood(MoodRequest(mood, reason, email)))
            .thenReturn(Response.error(400, "{}".toResponseBody("application/json".toMediaTypeOrNull())))

        val result = moodRepository.postMood(mood, reason, email)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error 400") == true)
    }

    @Test
    fun `postMood network error returns failure result`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"

        `when`(mockWebService.postMood(MoodRequest(mood, reason, email)))
            .thenThrow(IOException("Network error"))

        val result = moodRepository.postMood(mood, reason, email)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertEquals("Network error. Please check your connection.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `postMood http exception returns failure result`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"

        val httpException = HttpException(Response.error<MoodResponse>(500, "{}".toResponseBody("application/json".toMediaTypeOrNull())))
        `when`(mockWebService.postMood(MoodRequest(mood, reason, email)))
            .thenThrow(httpException)

        val result = moodRepository.postMood(mood, reason, email)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertTrue(result.exceptionOrNull()?.message?.contains("HTTP Error 500") == true)
    }

    @Test
    fun `getAllMoods success returns success result`() = runTest {
        val email = "test@example.com"
        val mockMoods = listOf(
            MoodData("Happy", "Good day", System.currentTimeMillis(), "id1", email),
            MoodData("Sad", "Bad day", System.currentTimeMillis(), "id2", email)
        )
        `when`(mockWebService.getMoodHistory(email))
            .thenReturn(Response.success(mockMoods))

        val result = moodRepository.getAllMoods(email)

        assertTrue(result.isSuccess)
        assertEquals(mockMoods, result.getOrNull())
    }

    @Test
    fun `getAllMoods unsuccessful response returns failure result`() = runTest {
        val email = "test@example.com"
        `when`(mockWebService.getMoodHistory(email))
            .thenReturn(Response.error(404, "{}".toResponseBody("application/json".toMediaTypeOrNull())))

        val result = moodRepository.getAllMoods(email)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertTrue(result.exceptionOrNull()?.message?.contains("Error 404") == true)
    }

    @Test
    fun `getAllMoods network error returns failure result`() = runTest {
        val email = "test@example.com"
        `when`(mockWebService.getMoodHistory(email))
            .thenThrow(IOException("Network error"))

        val result = moodRepository.getAllMoods(email)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertEquals("Network error. Please check your connection.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getAllMoods http exception returns failure result`() = runTest {
        val email = "test@example.com"
        val httpException = HttpException(Response.error<List<MoodData>>(500, "{}".toResponseBody("application/json".toMediaTypeOrNull())))
        `when`(mockWebService.getMoodHistory(email))
            .thenThrow(httpException)

        val result = moodRepository.getAllMoods(email)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
        assertTrue(result.exceptionOrNull()?.message?.contains("HTTP Error 500") == true)
    }
}

