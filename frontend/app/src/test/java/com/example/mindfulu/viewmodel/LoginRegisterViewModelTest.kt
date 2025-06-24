package com.example.mindfulu.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.UserResponse
import com.example.mindfulu.repository.AuthRepository
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
import org.mockito.Mockito.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class LoginRegisterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Ensures LiveData updates immediately

    private lateinit var loginRegisterViewModel: LoginRegisterViewModel

    @Mock
    private lateinit var mockAuthRepository: AuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        loginRegisterViewModel = LoginRegisterViewModel()

        // Manually inject the mock repository
        val field = LoginRegisterViewModel::class.java.getDeclaredField("authRepository")
        field.isAccessible = true
        field.set(loginRegisterViewModel, mockAuthRepository)
    }

    // --- Login Tests ---

    @Test
    fun `login successful updates loginResult and isLoading`() = runTest {
        val username = "testuser"
        val password = "hashedpassword"
        val mockUserResponse = UserResponse(1, username, "Test User", "test@example.com")
        val mockAuthResponse = AuthResponse("Login successful!", mockUserResponse)

        `when`(mockAuthRepository.loginWithFirestore(username, password))
            .thenReturn(Result.success(mockAuthResponse))

        // Initial state check
        assertEquals(false, loginRegisterViewModel.isLoading.value)

        loginRegisterViewModel.login(username, password)

        // Verify loading state changes
        assertEquals(true, loginRegisterViewModel.isLoading.value) // Loading starts
        assertEquals(false, loginRegisterViewModel.isLoading.value) // Loading ends

        // Verify loginResult and error are updated correctly
        assertEquals(mockAuthResponse, loginRegisterViewModel.loginResult.value)
        assertNull(loginRegisterViewModel.error.value)
        verify(mockAuthRepository).loginWithFirestore(username, password)
    }

    @Test
    fun `login failure updates error and isLoading`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"

        `when`(mockAuthRepository.loginWithFirestore(username, password))
            .thenReturn(Result.failure(Exception(errorMessage)))

        loginRegisterViewModel.login(username, password)

        // Verify loading state changes
        assertEquals(true, loginRegisterViewModel.isLoading.value) // Loading starts
        assertEquals(false, loginRegisterViewModel.isLoading.value) // Loading ends

        // Verify error is updated
        assertEquals(errorMessage, loginRegisterViewModel.error.value)
        assertNull(loginRegisterViewModel.loginResult.value)
        verify(mockAuthRepository).loginWithFirestore(username, password)
    }

    // --- Register Tests ---

    @Test
    fun `register successful updates registerResult and isLoading`() = runTest {
        val username = "newuser"
        val name = "New User"
        val email = "new@example.com"
        val password = "hashedpassword"
        val cpassword = "hashedpassword"
        val mockUserResponse = UserResponse(0, username, name, email)
        val mockAuthResponse = AuthResponse("Registration successful!", mockUserResponse)

        `when`(mockAuthRepository.registerWithFirestore(username, name, email, password, cpassword))
            .thenReturn(Result.success(mockAuthResponse))

        // Initial state check
        assertEquals(false, loginRegisterViewModel.isLoading.value)

        loginRegisterViewModel.register(username, name, email, password, cpassword)

        // Verify loading state changes
        assertEquals(true, loginRegisterViewModel.isLoading.value) // Loading starts
        assertEquals(false, loginRegisterViewModel.isLoading.value) // Loading ends

        // Verify registerResult and error are updated correctly
        assertEquals(mockAuthResponse, loginRegisterViewModel.registerResult.value)
        assertNull(loginRegisterViewModel.error.value)
        verify(mockAuthRepository).registerWithFirestore(username, name, email, password, cpassword)
    }

    @Test
    fun `register failure updates error and isLoading`() = runTest {
        val username = "existinguser"
        val name = "Existing User"
        val email = "existing@example.com"
        val password = "password"
        val cpassword = "password"
        val errorMessage = "Username already exists."

        `when`(mockAuthRepository.registerWithFirestore(username, name, email, password, cpassword))
            .thenReturn(Result.failure(Exception(errorMessage)))

        loginRegisterViewModel.register(username, name, email, password, cpassword)

        // Verify loading state changes
        assertEquals(true, loginRegisterViewModel.isLoading.value) // Loading starts
        assertEquals(false, loginRegisterViewModel.isLoading.value) // Loading ends

        // Verify error is updated
        assertEquals(errorMessage, loginRegisterViewModel.error.value)
        assertNull(loginRegisterViewModel.registerResult.value)
        verify(mockAuthRepository).registerWithFirestore(username, name, email, password, cpassword)
    }
}