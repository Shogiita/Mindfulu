package com.example.mindfulu.repository

import android.content.Context
import com.example.mindfulu.App
import com.example.mindfulu.AppDatabase
import com.example.mindfulu.RecommendationDao
import com.example.mindfulu.entity.RecommendationCacheEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.Calendar

@ExperimentalCoroutinesApi
class RecommendationCacheRepositoryTest {

    private lateinit var recommendationCacheRepository: RecommendationCacheRepository

    @Mock
    private lateinit var mockContext: Context
    @Mock
    private lateinit var mockAppDatabase: AppDatabase
    @Mock
    private lateinit var mockRecommendationDao: RecommendationDao

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock AppDatabase and its DAO
        `when`(mockAppDatabase.recommendationDao()).thenReturn(mockRecommendationDao)

        // Use reflection to set the mock AppDatabase instance for the singleton
        val getInstanceMethod = AppDatabase.Companion::class.java.getDeclaredMethod("getInstance", Context::class.java)
        getInstanceMethod.isAccessible = true
        getInstanceMethod.invoke(null, mockContext) // Call it once to set the instance if it uses internal singleton logic

        recommendationCacheRepository = RecommendationCacheRepository(mockContext)
    }

    @Test
    fun `saveRecommendation inserts into DAO`() = runTest {
        val recommendation = RecommendationCacheEntity(userEmail = "test@example.com", date = 1L, suggestionsJson = "{}")
        recommendationCacheRepository.saveRecommendation(recommendation)
        verify(mockRecommendationDao).insertRecommendation(recommendation)
    }

    @Test
    fun `getRecommendationForToday returns cached data if available`() = runTest {
        val email = "test@example.com"
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val mockRecommendation = RecommendationCacheEntity(userEmail = email, date = today, suggestionsJson = "{'test': 'data'}")
        `when`(mockRecommendationDao.getRecommendationForDate(email, today)).thenReturn(mockRecommendation)

        val result = recommendationCacheRepository.getRecommendationForToday(email)

        assertEquals(mockRecommendation, result)
        verify(mockRecommendationDao).getRecommendationForDate(email, today)
    }

    @Test
    fun `getRecommendationForToday returns null if no cached data for today`() = runTest {
        val email = "test@example.com"
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        `when`(mockRecommendationDao.getRecommendationForDate(email, today)).thenReturn(null)

        val result = recommendationCacheRepository.getRecommendationForToday(email)

        assertNull(result)
        verify(mockRecommendationDao).getRecommendationForDate(email, today)
    }

    @Test
    fun `cleanOldRecommendations calls deleteOldRecommendations on DAO`() = runTest {
        val email = "test@example.com"
        val daysAgo = 7

        recommendationCacheRepository.cleanOldRecommendations(email, daysAgo)

        // Capture the argument passed to verify, ensuring it's a timestamp
        verify(mockRecommendationDao).deleteOldRecommendations(email, anyLong())
    }
}