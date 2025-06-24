package com.example.mindfulu.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.mindfulu.data.ActivitySuggestion
import com.example.mindfulu.data.MusicSuggestion
import com.example.mindfulu.data.SuggestionResponse
import com.example.mindfulu.data.Suggestions
import com.example.mindfulu.entity.RecommendationCacheEntity
import com.example.mindfulu.repository.SuggestionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

@ExperimentalCoroutinesApi
class SuggestionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var mockApplication: Application
    @Mock
    private lateinit var suggestionRepository: SuggestionRepository
    @Mock
    private lateinit var cacheRepository: RecommendationCacheRepository

    @Mock
    private lateinit var suggestionsObserver: Observer<SuggestionResponse?>
    @Mock
    private lateinit var errorObserver: Observer<String>
    @Mock
    private lateinit var isLoadingObserver: Observer<Boolean>

    private lateinit var viewModel: SuggestionViewModel

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val suggestionResponseAdapter = moshi.adapter(SuggestionResponse::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = SuggestionViewModel(mockApplication)
        // Using reflection to inject mocked repositories
        val suggestionRepoField = SuggestionViewModel::class.java.getDeclaredField("suggestionRepository")
        suggestionRepoField.isAccessible = true
        suggestionRepoField.set(viewModel, suggestionRepository)

        val cacheRepoField = SuggestionViewModel::class.java.getDeclaredField("cacheRepository")
        cacheRepoField.isAccessible = true
        cacheRepoField.set(viewModel, cacheRepository)

        // Mock App.moshi if it's a static field accessed directly in ViewModel
        // This is tricky and usually indicates a need for dependency injection for Moshi too.
        // For simplicity, we'll assume Moshi is correctly initialized via App for this test's purpose.

        viewModel.suggestions.observeForever(suggestionsObserver)
        viewModel.error.observeForever(errorObserver)
        viewModel.isLoading.observeForever(isLoadingObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()

        viewModel.suggestions.removeObserver(suggestionsObserver)
        viewModel.error.removeObserver(errorObserver)
        viewModel.isLoading.removeObserver(isLoadingObserver)
    }

    @Test
    fun `getSuggestions fetches from network, caches, and posts success`() = runTest {
        val mood = "Happy"
        val reason = "Great day"
        val email = "test@example.com"
        val music = MusicSuggestion("Song", "Artist", "Reason", "link")
        val activity = ActivitySuggestion("Activity", "Desc")
        val suggestionsData = Suggestions(music, listOf(activity))
        val apiResponse = SuggestionResponse("Success", suggestionsData)

        `when`(suggestionRepository.getSuggestions(mood, reason)).thenReturn(Result.success(apiResponse))
        `when`(cacheRepository.saveRecommendation(any())).thenReturn(Unit) // Mock saving

        viewModel.getSuggestions(mood, reason, email)

        verify(isLoadingObserver).onChanged(true)
        verify(suggestionRepository).getSuggestions(mood, reason)
        verify(cacheRepository).saveRecommendation(argThat { entity ->
            entity.userEmail == email && suggestionResponseAdapter.fromJson(entity.suggestionsJson) == apiResponse
        })
        verify(suggestionsObserver).onChanged(apiResponse)
        verify(isLoadingObserver).onChanged(false)
        verifyNoMoreInteractions(errorObserver)
    }

    @Test
    fun `getSuggestions posts error on network failure`() = runTest {
        val mood = "Happy"
        val reason = "Great day"
        val email = "test@example.com"
        val errorMessage = "Network Error"

        `when`(suggestionRepository.getSuggestions(mood, reason)).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.getSuggestions(mood, reason, email)

        verify(isLoadingObserver).onChanged(true)
        verify(suggestionRepository).getSuggestions(mood, reason)
        verify(errorObserver).onChanged(errorMessage)
        verify(isLoadingObserver).onChanged(false)
        verifyNoMoreInteractions(suggestionsObserver)
        verifyNoInteractions(cacheRepository)
    }

    @Test
    fun `loadSuggestionsForToday loads from cache if available`() = runTest {
        val email = "test@example.com"
        val mood = "Sad"
        val reason = "Tired"
        val cachedMusic = MusicSuggestion("Cached Song", "Cached Artist", "Cached Reason", null)
        val cachedActivity = ActivitySuggestion("Cached Activity", "Cached Desc")
        val cachedSuggestionsData = Suggestions(cachedMusic, listOf(cachedActivity))
        val cachedResponse = SuggestionResponse("Cached", cachedSuggestionsData)
        val cachedJson = suggestionResponseAdapter.toJson(cachedResponse)

        val dummyCacheEntity = RecommendationCacheEntity(
            id = 1L,
            userEmail = email,
            date = System.currentTimeMillis(), // Will be normalized to start of day by cache repo
            suggestionsJson = cachedJson
        )
        `when`(cacheRepository.getRecommendationForToday(email)).thenReturn(dummyCacheEntity)

        viewModel.loadSuggestionsForToday(email, mood, reason)

        verify(isLoadingObserver).onChanged(true)
        verify(cacheRepository).getRecommendationForToday(email)
        verify(suggestionsObserver).onChanged(cachedResponse)
        verify(isLoadingObserver).onChanged(false)
        verifyNoInteractions(suggestionRepository) // Should not call network
        verifyNoMoreInteractions(errorObserver)
    }

    @Test
    fun `loadSuggestionsForToday fetches from network if cache is empty`() = runTest {
        val email = "test@example.com"
        val mood = "Happy"
        val reason = "Good"
        val apiMusic = MusicSuggestion("API Song", "API Artist", "API Reason", "api_link")
        val apiActivity = ActivitySuggestion("API Activity", "API Desc")
        val apiSuggestionsData = Suggestions(apiMusic, listOf(apiActivity))
        val apiResponse = SuggestionResponse("Fetched", apiSuggestionsData)

        `when`(cacheRepository.getRecommendationForToday(email)).thenReturn(null) // Cache miss
        `when`(suggestionRepository.getSuggestions(mood, reason)).thenReturn(Result.success(apiResponse))
        `when`(cacheRepository.saveRecommendation(any())).thenReturn(Unit)

        viewModel.loadSuggestionsForToday(email, mood, reason)

        verify(isLoadingObserver).onChanged(true)
        verify(cacheRepository).getRecommendationForToday(email)
        verify(suggestionRepository).getSuggestions(mood, reason) // Network call
        verify(cacheRepository).saveRecommendation(any()) // Cache after fetch
        verify(suggestionsObserver).onChanged(apiResponse)
        verify(isLoadingObserver).onChanged(false)
        verifyNoMoreInteractions(errorObserver)
    }

    @Test
    fun `loadSuggestionsForToday posts error if cache deserialization fails`() = runTest {
        val email = "test@example.com"
        val mood = "Mood"
        val reason = "Reason"
        val invalidJson = "invalid json string"
        val dummyCacheEntity = RecommendationCacheEntity(
            id = 1L,
            userEmail = email,
            date = System.currentTimeMillis(),
            suggestionsJson = invalidJson // Malformed JSON
        )

        `when`(cacheRepository.getRecommendationForToday(email)).thenReturn(dummyCacheEntity)
        `when`(suggestionRepository.getSuggestions(mood, reason)).thenReturn(Result.failure(Exception("Network error on fallback"))) // Mock fallback network call error

        viewModel.loadSuggestionsForToday(email, mood, reason)

        verify(isLoadingObserver).onChanged(true)
        verify(cacheRepository).getRecommendationForToday(email)
        // Verify that it attempts to fetch from network on deserialization failure
        verify(suggestionRepository).getSuggestions(mood, reason)
        verify(errorObserver).onChanged("Network error on fallback") // Error from the fallback network call
        verify(isLoadingObserver, times(2)).onChanged(false) // Initial false, then after network error
        verifyNoMoreInteractions(suggestionsObserver)
    }
}