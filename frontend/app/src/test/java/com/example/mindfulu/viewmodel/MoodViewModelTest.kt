package com.example.mindfulu.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.mindfulu.MoodData
import com.example.mindfulu.data.MoodResponse
import com.example.mindfulu.repository.MoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.net.ConnectException
import java.util.concurrent.TimeoutException

@ExperimentalCoroutinesApi
class MoodViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var moodRepository: MoodRepository

    @Mock
    private lateinit var moodResultObserver: Observer<MoodResponse>
    @Mock
    private lateinit var moodHistoryObserver: Observer<List<MoodData>>
    @Mock
    private lateinit var errorObserver: Observer<String>
    @Mock
    private lateinit var isLoadingObserver: Observer<Boolean>

    private lateinit var viewModel: MoodViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = MoodViewModel()
        // Using reflection to inject the mock repository
        val moodRepoField = MoodViewModel::class.java.getDeclaredField("moodRepository")
        moodRepoField.isAccessible = true
        moodRepoField.set(viewModel, moodRepository)

        viewModel.moodResult.observeForever(moodResultObserver)
        viewModel.moodHistory.observeForever(moodHistoryObserver)
        viewModel.error.observeForever(errorObserver)
        viewModel.isLoading.observeForever(isLoadingObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()

        viewModel.moodResult.removeObserver(moodResultObserver)
        viewModel.moodHistory.removeObserver(moodHistoryObserver)
        viewModel.error.removeObserver(errorObserver)
        viewModel.isLoading.removeObserver(isLoadingObserver)
    }

    @Test
    fun `postMood sets isLoading to true then false and posts success result`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        val successResponse = MoodResponse("Success", MoodData(mood, reason, 1L, "id", email))
        `when`(moodRepository.postMood(mood, reason, email)).thenReturn(Result.success(successResponse))

        viewModel.postMood(mood, reason, email)

        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)
        verify(moodResultObserver).onChanged(successResponse)
        verifyNoMoreInteractions(errorObserver)
    }

    @Test
    fun `postMood posts specific error message for ConnectException`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        `when`(moodRepository.postMood(mood, reason, email)).thenReturn(Result.failure(ConnectException("Failed to connect")))

        viewModel.postMood(mood, reason, email)

        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)
        verify(errorObserver).onChanged("Cannot connect to server. Please check your internet connection and server status.")
        verifyNoMoreInteractions(moodResultObserver)
    }

    @Test
    fun `postMood posts specific error message for SocketTimeoutException`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        `when`(moodRepository.postMood(mood, reason, email)).thenReturn(Result.failure(TimeoutException("SocketTimeoutException occurred")))

        viewModel.postMood(mood, reason, email)

        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)
        verify(errorObserver).onChanged("Connection timeout. Please try again.")
        verifyNoMoreInteractions(moodResultObserver)
    }

    @Test
    fun `postMood posts generic error message for other exceptions`() = runTest {
        val mood = "Happy"
        val reason = "Good day"
        val email = "test@example.com"
        `when`(moodRepository.postMood(mood, reason, email)).thenReturn(Result.failure(RuntimeException("Something unexpected")))

        viewModel.postMood(mood, reason, email)

        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)
        verify(errorObserver).onChanged("Failed to post mood") // General failure message from repository
        verifyNoMoreInteractions(moodResultObserver)
    }


    @Test
    fun `getAllMoods sets isLoading to true then false and posts history`() = runTest {
        val email = "test@example.com"
        val moodList = listOf(MoodData("Happy", "Test", 1L, "id", email))
        `when`(moodRepository.getAllMoods(email)).thenReturn(Result.success(moodList))

        viewModel.getAllMoods(email)

        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)
        verify(moodHistoryObserver).onChanged(moodList)
        verifyNoMoreInteractions(errorObserver)
    }

    @Test
    fun `getAllMoods posts specific error message for ConnectException`() = runTest {
        val email = "test@example.com"
        `when`(moodRepository.getAllMoods(email)).thenReturn(Result.failure(ConnectException("No connection")))

        viewModel.getAllMoods(email)

        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)
        verify(errorObserver).onChanged("Cannot connect to server. Please check your internet connection and server status.")
        verifyNoMoreInteractions(moodHistoryObserver)
    }
}