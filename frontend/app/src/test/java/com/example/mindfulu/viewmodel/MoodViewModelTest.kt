package com.example.mindfulu.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mindfulu.MoodData
import com.example.mindfulu.data.MoodResponse
import com.example.mindfulu.repository.MoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class MoodViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Ensures LiveData updates immediately

    private lateinit var moodViewModel: MoodViewModel

    @Mock
    private lateinit var mockMoodRepository: MoodRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        moodViewModel = MoodViewModel()
        // Manually inject the mock repository
        val field = MoodViewModel::class.java.getDeclaredField("moodRepository")
        field.isAccessible = true
        field.set(moodViewModel, mockMoodRepository)
    }

    @Test
    fun `postMood success updates moodResult and isLoading`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        val mockMoodData = MoodData(mood, reason, System.currentTimeMillis(), "id123", email)
        val mockResponse = MoodResponse("Mood saved successfully", mockMoodData)

        `when`(mockMoodRepository.postMood(mood, reason, email))
            .thenReturn(Result.success(mockResponse))

        // Initial state check
        assertEquals(false, moodViewModel.isLoading.value)

        moodViewModel.postMood(mood, reason, email)

        // Verify loading state changes
        assertEquals(true, moodViewModel.isLoading.value) // Loading starts
        // After coroutine finishes, loading should be false
        assertEquals(false, moodViewModel.isLoading.value)

        // Verify moodResult and error are updated correctly
        assertEquals(mockResponse, moodViewModel.moodResult.value)
        assertEquals(null, moodViewModel.error.value)
        verify(mockMoodRepository).postMood(mood, reason, email)
    }

    @Test
    fun `postMood network error updates error and isLoading`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        val errorMessage = "Cannot connect to server. Please check your internet connection and server status."

        `when`(mockMoodRepository.postMood(mood, reason, email))
            .thenReturn(Result.failure(Exception("ConnectException")))

        moodViewModel.postMood(mood, reason, email)

        // Verify loading state changes
        assertEquals(true, moodViewModel.isLoading.value) // Loading starts
        // After coroutine finishes, loading should be false
        assertEquals(false, moodViewModel.isLoading.value)

        // Verify moodResult and error are updated correctly
        assertEquals(null, moodViewModel.moodResult.value)
        assertEquals(errorMessage, moodViewModel.error.value)
        verify(mockMoodRepository).postMood(mood, reason, email)
    }

    @Test
    fun `postMood timeout error updates error and isLoading`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        val errorMessage = "Connection timeout. Please try again."

        `when`(mockMoodRepository.postMood(mood, reason, email))
            .thenReturn(Result.failure(Exception("SocketTimeoutException")))

        moodViewModel.postMood(mood, reason, email)

        assertEquals(errorMessage, moodViewModel.error.value)
        assertEquals(false, moodViewModel.isLoading.value)
    }

    @Test
    fun `postMood generic error updates error and isLoading`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        val errorMessage = "Something went wrong"

        `when`(mockMoodRepository.postMood(mood, reason, email))
            .thenReturn(Result.failure(Exception(errorMessage)))

        moodViewModel.postMood(mood, reason, email)

        assertEquals(errorMessage, moodViewModel.error.value)
        assertEquals(false, moodViewModel.isLoading.value)
    }

    @Test
    fun `getAllMoods success updates moodHistory and isLoading`() = runTest {
        val email = "test@example.com"
        val mockMoods = listOf(MoodData("Happy", "Good day", System.currentTimeMillis(), "id1", email))

        `when`(mockMoodRepository.getAllMoods(email))
            .thenReturn(Result.success(mockMoods))

        // Initial state check
        assertEquals(false, moodViewModel.isLoading.value)
        assertTrue(moodViewModel.moodHistory.value.isNullOrEmpty())

        moodViewModel.getAllMoods(email)

        // Verify loading state changes
        assertEquals(true, moodViewModel.isLoading.value) // Loading starts
        // After coroutine finishes, loading should be false
        assertEquals(false, moodViewModel.isLoading.value)

        // Verify moodHistory and error are updated correctly
        assertEquals(mockMoods, moodViewModel.moodHistory.value)
        assertEquals(null, moodViewModel.error.value)
        verify(mockMoodRepository).getAllMoods(email)
    }

    @Test
    fun `getAllMoods network error updates error and isLoading`() = runTest {
        val email = "test@example.com"
        val errorMessage = "Cannot connect to server. Please check your internet connection and server status."

        `when`(mockMoodRepository.getAllMoods(email))
            .thenReturn(Result.failure(Exception("ConnectException")))

        moodViewModel.getAllMoods(email)

        // Verify loading state changes
        assertEquals(true, moodViewModel.isLoading.value) // Loading starts
        // After coroutine finishes, loading should be false
        assertEquals(false, moodViewModel.isLoading.value)

        // Verify moodHistory and error are updated correctly
        assertEquals(null, moodViewModel.moodHistory.value)
        assertEquals(errorMessage, moodViewModel.error.value)
        verify(mockMoodRepository).getAllMoods(email)
    }

    @Test
    fun `getAllMoods timeout error updates error and isLoading`() = runTest {
        val email = "test@example.com"
        val errorMessage = "Connection timeout. Please try again."

        `when`(mockMoodRepository.getAllMoods(email))
            .thenReturn(Result.failure(Exception("SocketTimeoutException")))

        moodViewModel.getAllMoods(email)

        assertEquals(errorMessage, moodViewModel.error.value)
        assertEquals(false, moodViewModel.isLoading.value)
    }

    @Test
    fun `getAllMoods generic error updates error and isLoading`() = runTest {
        val email = "test@example.com"
        val errorMessage = "Failed to fetch mood history"

        `when`(mockMoodRepository.getAllMoods(email))
            .thenReturn(Result.failure(Exception(errorMessage)))

        moodViewModel.getAllMoods(email)

        assertEquals(errorMessage, moodViewModel.error.value)
        assertEquals(false, moodViewModel.isLoading.value)
    }
}