package com.example.mindfulu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AppTest {

    @Test
    fun `hashPassword returns consistent hash for same input`() {
        val password = "mySecretPassword123"
        val hashedPassword1 = App.hashPassword(password)
        val hashedPassword2 = App.hashPassword(password)
        assertEquals("Hashed passwords should be identical for the same input", hashedPassword1, hashedPassword2)
    }

    @Test
    fun `hashPassword returns different hash for different input`() {
        val passwordOne = "mySecretPassword123"
        val passwordTwo = "anotherSecretPassword456"
        val hashedPasswordOne = App.hashPassword(passwordOne)
        val hashedPasswordTwo = App.hashPassword(passwordTwo)
        assertNotEquals("Hashed passwords should be different for different inputs", hashedPasswordOne, hashedPasswordTwo)
    }

    @Test
    fun `isSameDay returns true for timestamps on the same day`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JUNE, 24, 10, 0, 0)
        val timestamp1 = calendar.timeInMillis

        calendar.set(2025, Calendar.JUNE, 24, 15, 30, 0)
        val timestamp2 = calendar.timeInMillis

        assertTrue("Timestamps on the same day should return true", App.isSameDay(timestamp1, timestamp2))
    }

    @Test
    fun `isSameDay returns false for timestamps on different days of the same month`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JUNE, 24, 10, 0, 0)
        val timestamp1 = calendar.timeInMillis

        calendar.set(2025, Calendar.JUNE, 25, 10, 0, 0)
        val timestamp2 = calendar.timeInMillis

        assertFalse("Timestamps on different days should return false", App.isSameDay(timestamp1, timestamp2))
    }

    @Test
    fun `isSameDay returns false for timestamps in different months`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JUNE, 24, 10, 0, 0)
        val timestamp1 = calendar.timeInMillis

        calendar.set(2025, Calendar.JULY, 24, 10, 0, 0)
        val timestamp2 = calendar.timeInMillis

        assertFalse("Timestamps in different months should return false", App.isSameDay(timestamp1, timestamp2))
    }

    @Test
    fun `isSameDay returns false for timestamps in different years`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JUNE, 24, 10, 0, 0)
        val timestamp1 = calendar.timeInMillis

        calendar.set(2026, Calendar.JUNE, 24, 10, 0, 0)
        val timestamp2 = calendar.timeInMillis

        assertFalse("Timestamps in different years should return false", App.isSameDay(timestamp1, timestamp2))
    }
}