package com.example.mindfulu.repository

import android.content.Context
import com.example.mindfulu.AppDatabase
import com.example.mindfulu.UserSessionDao
import com.example.mindfulu.entity.UserSessionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SessionRepositoryTest {

    private lateinit var sessionRepository: SessionRepository

    @Mock
    private lateinit var mockContext: Context
    @Mock
    private lateinit var mockAppDatabase: AppDatabase
    @Mock
    private lateinit var mockUserSessionDao: UserSessionDao

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock AppDatabase and its DAO
        `when`(mockAppDatabase.userSessionDao()).thenReturn(mockUserSessionDao)
        // Mock AppDatabase.getInstance()
        val getInstanceMethod = AppDatabase.Companion::class.java.getDeclaredMethod("getInstance", Context::class.java)
        getInstanceMethod.isAccessible = true
        getInstanceMethod.invoke(null, mockContext) // Call it once to set the instance if it uses internal singleton logic

        sessionRepository = SessionRepository(mockContext)
    }

    @Test
    fun `saveUserSession inserts into DAO`() = runTest {
        val session = UserSessionEntity("test@example.com", true, 1L)
        sessionRepository.saveUserSession(session)
        verify(mockUserSessionDao).insertUserSession(session)
    }

    @Test
    fun `getActiveUserSession returns active session from DAO`() = runTest {
        val activeSession = UserSessionEntity("active@example.com", true, 1L)
        `when`(mockUserSessionDao.getActiveSession()).thenReturn(activeSession)

        val result = sessionRepository.getActiveUserSession()
        assertEquals(activeSession, result)
        verify(mockUserSessionDao).getActiveSession()
    }

    @Test
    fun `getActiveUserSession returns null if no active session`() = runTest {
        `when`(mockUserSessionDao.getActiveSession()).thenReturn(null)

        val result = sessionRepository.getActiveUserSession()
        assertNull(result)
        verify(mockUserSessionDao).getActiveSession()
    }

    @Test
    fun `deleteUserSession deletes session from DAO`() = runTest {
        val email = "delete@example.com"
        sessionRepository.deleteUserSession(email)
        verify(mockUserSessionDao).deleteUserSession(email)
    }
}