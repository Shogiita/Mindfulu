package com.example.mindfulu.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.UserResponse
import com.example.mindfulu.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class LoginRegisterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Ensures LiveData updates immediately

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var authRepository: AuthRepository

    // Observers for LiveData
    @Mock
    private lateinit var registerResultObserver: Observer<AuthResponse>
    @Mock
    private lateinit var loginResultObserver: Observer<AuthResponse>
    @Mock
    private lateinit var errorObserver: Observer<String>
    @Mock
    private lateinit var isLoadingObserver: Observer<Boolean>

    private lateinit var viewModel: LoginRegisterViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher) // Set Main dispatcher for coroutines

        viewModel = LoginRegisterViewModel()
        // Manually set the mocked repository if not using constructor injection
        // This requires reflection or a setter in LoginRegisterViewModel, or direct instantiation in test
        // For this example, let's assume we can set it. A better practice is dependency injection.
        val authRepoField = LoginRegisterViewModel::class.java.getDeclaredField("authRepository")
        authRepoField.isAccessible = true
        authRepoField.set(viewModel, authRepository)

        // Observe LiveData
        viewModel.registerResult.observeForever(registerResultObserver)
        viewModel.loginResult.observeForever(loginResultObserver)
        viewModel.error.observeForever(errorObserver)
        viewModel.isLoading.observeForever(isLoadingObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset Main dispatcher
        testDispatcher.cleanupTestCoroutines()

        // Remove observers to prevent leaks
        viewModel.registerResult.removeObserver(registerResultObserver)
        viewModel.loginResult.removeObserver(loginResultObserver)
        viewModel.error.removeObserver(errorObserver)
        viewModel.isLoading.removeObserver(isLoadingObserver)
    }

    @Test
    fun `login sets isLoading to true then false and posts success result`() = runTest {
        val successResponse = AuthResponse("Login successful!", UserResponse(1, "user", "name", "email"))
        `when`(authRepository.loginWithFirestore("testuser", "hashedpass")).thenReturn(Result.success(successResponse))

        viewModel.login("testuser", "hashedpass")

        // Verify loading states
        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)

        // Verify success result
        verify(loginResultObserver).onChanged(successResponse)
        verifyNoMoreInteractions(errorObserver) // Ensure no error was posted
    }

    @Test
    fun `login posts error message on failure`() = runTest {
        val errorMessage = "Invalid credentials"
        `when`(authRepository.loginWithFirestore("testuser", "hashedpass")).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.login("testuser", "hashedpass")

        // Verify loading states
        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)

        // Verify error result
        verify(errorObserver).onChanged(errorMessage)
        verifyNoMoreInteractions(loginResultObserver) // Ensure no success was posted
    }

    @Test
    fun `register sets isLoading to true then false and posts success result`() = runTest {
        val successResponse = AuthResponse("Registration successful!", UserResponse(0, "newuser", "New User", "new@example.com"))
        `when`(authRepository.registerWithFirestore("newuser", "New User", "new@example.com", "hashedpass", "hashedpass")).thenReturn(Result.success(successResponse))

        viewModel.register("newuser", "New User", "new@example.com", "hashedpass", "hashedpass")

        // Verify loading states
        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)

        // Verify success result
        verify(registerResultObserver).onChanged(successResponse)
        verifyNoMoreInteractions(errorObserver) // Ensure no error was posted
    }

    @Test
    fun `register posts error message on failure`() = runTest {
        val errorMessage = "Email already exists."
        `when`(authRepository.registerWithFirestore("newuser", "New User", "new@example.com", "hashedpass", "hashedpass")).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.register("newuser", "New User", "new@example.com", "hashedpass", "hashedpass")

        // Verify loading states
        verify(isLoadingObserver).onChanged(true)
        verify(isLoadingObserver).onChanged(false)

        // Verify error result
        verify(errorObserver).onChanged(errorMessage)
        verifyNoMoreInteractions(registerResultObserver) // Ensure no success was posted
    }
}