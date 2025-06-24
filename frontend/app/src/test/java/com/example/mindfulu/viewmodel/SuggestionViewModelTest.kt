package com.example.mindfulu.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mindfulu.data.ActivitySuggestion
import com.example.mindfulu.data.MusicSuggestion
import com.example.mindfulu.data.SuggestionResponse
import com.example.mindfulu.data.Suggestions
import com.example.mindfulu.entity.RecommendationCacheEntity
import com.example.mindfulu.repository.RecommendationCacheRepository
import com.example.mindfulu.repository.SuggestionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SuggestionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Ensures LiveData updates immediately

    private lateinit var suggestionViewModel: SuggestionViewModel

    @Mock
    private lateinit var mockApplication: Application
    @Mock
    private lateinit var mockSuggestionRepository: SuggestionRepository
    @Mock
    private lateinit var mockRecommendationCacheRepository: RecommendationCacheRepository

    // Real Moshi instance for testing JSON serialization/deserialization
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val mockMusicSuggestion = MusicSuggestion("Test Song", "Test Artist", "For testing", null)
    private val mockActivitySuggestions = listOf(ActivitySuggestion("Test Activity", "Do something"))
    private val mockSuggestions = Suggestions(mockMusicSuggestion, mockActivitySuggestions)
    private val mockSuggestionResponse = SuggestionResponse("Success", mockSuggestions)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        suggestionViewModel = SuggestionViewModel(mockApplication)

        // Manually inject mocks
        val suggestionRepoField = SuggestionViewModel::class.java.getDeclaredField("suggestionRepository")
        suggestionRepoField.isAccessible = true
        suggestionRepoField.set(suggestionViewModel, mockSuggestionRepository)

        val cacheRepoField = SuggestionViewModel::class.java.getDeclaredField("cacheRepository")
        cacheRepoField.isAccessible = true
        cacheRepoField.set(suggestionViewModel, mockRecommendationCacheRepository)

        val moshiField = SuggestionViewModel::class.java.getDeclaredField("moshi")
        moshiField.isAccessible = true
        moshiField.set(suggestionViewModel, moshi)
    }

    @Test
    fun `getSuggestions success fetches and caches suggestions`() = runTest {
        val mood = "Happy"
        val reason = "Finished work"
        val email = "user@example.com"
        val jsonString = moshi.adapter(SuggestionResponse::class.java).toJson(mockSuggestionResponse)

        `when`(mockSuggestionRepository.getSuggestions(mood, reason))
            .thenReturn(Result.success(mockSuggestionResponse))

        suggestionViewModel.getSuggestions(mood, reason, email)

        // Verify isLoading
        assertEquals(true, suggestionViewModel.isLoading.value) // Loading starts
        assertEquals(false, suggestionViewModel.isLoading.value) // Loading ends

        // Verify _suggestions LiveData
        assertEquals(mockSuggestionResponse, suggestionViewModel.suggestions.value)

        // Verify caching happened
        verify(mockRecommendationCacheRepository).saveRecommendation(
            any(RecommendationCacheEntity::class.java)
        )
        assertNull(suggestionViewModel.error.value)
    }

    @Test
    fun `getSuggestions failure updates error LiveData`() = runTest {
        val mood = "Sad"
        val reason = "Lost game"
        val email = "user@example.com"
        val errorMessage = "Failed to fetch suggestions"

        `when`(mockSuggestionRepository.getSuggestions(mood, reason))
            .thenReturn(Result.failure(Exception(errorMessage)))

        suggestionViewModel.getSuggestions(mood, reason, email)

        // Verify isLoading
        assertEquals(true, suggestionViewModel.isLoading.value)
        assertEquals(false, suggestionViewModel.isLoading.value)

        // Verify _error LiveData
        assertEquals(errorMessage, suggestionViewModel.error.value)
        assertNull(suggestionViewModel.suggestions.value)

        // Verify no caching happened
        verify(mockRecommendationCacheRepository, never()).saveRecommendation(any())
    }

    @Test
    fun `loadSuggestionsForToday returns cached data if available`() = runTest {
        val email = "cache@example.com"
        val mood = "Happy"
        val reason = "Finished work"
        val jsonString = moshi.adapter(SuggestionResponse::class.java).toJson(mockSuggestionResponse)
        val cachedEntity = RecommendationCacheEntity(userEmail = email, date = 0L, suggestionsJson = jsonString)

        `when`(mockRecommendationCacheRepository.getRecommendationForToday(email))
            .thenReturn(cachedEntity)

        suggestionViewModel.loadSuggestionsForToday(email, mood, reason)

        // Verify isLoading
        assertEquals(true, suggestionViewModel.isLoading.value)
        assertEquals(false, suggestionViewModel.isLoading.value)

        // Verify suggestions from cache
        assertEquals(mockSuggestionResponse, suggestionViewModel.suggestions.value)

        // Verify repository was NOT called
        verify(mockSuggestionRepository, never()).getSuggestions(anyString(), anyString())
        assertNull(suggestionViewModel.error.value)
    }

    @Test
    fun `loadSuggestionsForToday fetches from network if cache is empty`() = runTest {
        val email = "nocache@example.com"
        val mood = "Happy"
        val reason = "Finished work"
        val jsonString = moshi.adapter(SuggestionResponse::class.java).toJson(mockSuggestionResponse)

        `when`(mockRecommendationCacheRepository.getRecommendationForToday(email))
            .thenReturn(null)
        `when`(mockSuggestionRepository.getSuggestions(mood, reason))
            .thenReturn(Result.success(mockSuggestionResponse))

        suggestionViewModel.loadSuggestionsForToday(email, mood, reason)

        // Verify isLoading
        assertEquals(true, suggestionViewModel.isLoading.value)
        assertEquals(false, suggestionViewModel.isLoading.value)

        // Verify suggestions from network
        assertEquals(mockSuggestionResponse, suggestionViewModel.suggestions.value)

        // Verify repository was called
        verify(mockSuggestionRepository).getSuggestions(mood, reason)
        // Verify caching happened after network fetch
        verify(mockRecommendationCacheRepository).saveRecommendation(any())
        assertNull(suggestionViewModel.error.value)
    }

    @Test
    fun `loadSuggestionsForToday handles network error when cache is empty`() = runTest {
        val email = "error@example.com"
        val mood = "Sad"
        val reason = "Broken phone"
        val errorMessage = "Network error"

        `when`(mockRecommendationCacheRepository.getRecommendationForToday(email))
            .thenReturn(null)
        `when`(mockSuggestionRepository.getSuggestions(mood, reason))
            .thenReturn(Result.failure(Exception(errorMessage)))

        suggestionViewModel.loadSuggestionsForToday(email, mood, reason)

        // Verify isLoading
        assertEquals(true, suggestionViewModel.isLoading.value)
        assertEquals(false, suggestionViewModel.isLoading.value)

        // Verify error LiveData
        assertEquals(errorMessage, suggestionViewModel.error.value)
        assertNull(suggestionViewModel.suggestions.value)

        // Verify repository was called
        verify(mockSuggestionRepository).getSuggestions(mood, reason)
        // Verify no caching happened
        verify(mockRecommendationCacheRepository, never()).saveRecommendation(any())
    }

    @Test
    fun `loadSuggestionsForToday handles deserialization error from cache`() = runTest {
        val email = "invalidjson@example.com"
        val mood = "Happy"
        val reason = "Finished work"
        val invalidJson = "invalid json string" // Malformed JSON
        val cachedEntity = RecommendationCacheEntity(userEmail = email, date = 0L, suggestionsJson = invalidJson)

        `when`(mockRecommendationCacheRepository.getRecommendationForToday(email))
            .thenReturn(cachedEntity)
        // Ensure network call happens if deserialization fails
        `when`(mockSuggestionRepository.getSuggestions(mood, reason))
            .thenReturn(Result.success(mockSuggestionResponse))

        suggestionViewModel.loadSuggestionsForToday(email, mood, reason)

        // Verify suggestions from network (fallback)
        assertEquals(mockSuggestionResponse, suggestionViewModel.suggestions.value)
        verify(mockSuggestionRepository).getSuggestions(mood, reason)
        assertNull(suggestionViewModel.error.value)
    }
}