package com.example.mindfulu.repository

import android.app.Application
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
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.Calendar

@ExperimentalCoroutinesApi
class RecommendationCacheRepositoryTest {

    private lateinit var repository: RecommendationCacheRepository

    @Mock
    private lateinit var mockApplicationContext: Application
    @Mock
    private lateinit var mockAppDatabase: AppDatabase
    @Mock
    private lateinit var mockRecommendationDao: RecommendationDao

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock AppDatabase and its DAO
        `when`(mockAppDatabase.recommendationDao()).thenReturn(mockRecommendationDao)
        // Mock AppDatabase.getInstance() to return our mocked instance
        // This is usually tricky for static methods; for a proper unit test,
        // AppDatabase should be injected or its getInstance() method can be mocked
        // using PowerMock or by making getInstance non-static (better for testability).
        // For demonstration, let's assume we can control AppDatabase.getInstance().
        val getInstanceMethod = AppDatabase.Companion::class.java.getDeclaredMethod("getInstance", Application::class.java)
        getInstanceMethod.isAccessible = true
        getInstanceMethod.invoke(null, mockApplicationContext) // Call it once to set the instance if it uses internal singleton logic

        repository = RecommendationCacheRepository(mockApplicationContext)
    }

    // Note: The `AppDatabase.getInstance` static method makes true unit testing difficult
    // without PowerMock or refactoring to inject AppDatabase.
    // The current setup here assumes a way to control the singleton for testing.

    @Test
    fun `saveRecommendation inserts data into DAO`() = runTest {
        val entity = RecommendationCacheEntity(userEmail = "test@example.com", date = 1L, suggestionsJson = "{}")
        repository.saveRecommendation(entity)
        verify(mockRecommendationDao).insertRecommendation(entity)
    }

    @Test
    fun `getRecommendationForToday returns cached data if available`() = runTest {
        val email = "test@example.com"
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val expectedEntity = RecommendationCacheEntity(userEmail = email, date = todayStart, suggestionsJson = "{}")

        `when`(mockRecommendationDao.getRecommendationForDate(email, todayStart)).thenReturn(expectedEntity)

        val result = repository.getRecommendationForToday(email)
        assertEquals(expectedEntity, result)
        verify(mockRecommendationDao).getRecommendationForDate(email, todayStart)
    }

    @Test
    fun `getRecommendationForToday returns null if no cached data`() = runTest {
        val email = "test@example.com"
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        `when`(mockRecommendationDao.getRecommendationForDate(email, todayStart)).thenReturn(null)

        val result = repository.getRecommendationForToday(email)
        assertNull(result)
        verify(mockRecommendationDao).getRecommendationForDate(email, todayStart)
    }

    @Test
    fun `cleanOldRecommendations calls DAO to delete old data`() = runTest {
        val email = "test@example.com"
        val daysAgo = 7
        val threshold = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }.timeInMillis

        repository.cleanOldRecommendations(email, daysAgo)

        verify(mockRecommendationDao).deleteOldRecommendations(eq(email), anyLong()) // Use anyLong() for time-dependent arg
    }
}