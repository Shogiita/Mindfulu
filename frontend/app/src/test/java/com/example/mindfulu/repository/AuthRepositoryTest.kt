package com.example.mindfulu.repository

import com.example.mindfulu.App
import com.example.mindfulu.WebService
import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.LoginRequest
import com.example.mindfulu.data.RegisterRequest
import com.example.mindfulu.data.UserResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var mockWebService: WebService // Although not directly used by the tested methods, it's a dependency in the original AuthRepository
    @Mock
    private lateinit var mockFirestore: FirebaseFirestore
    @Mock
    private lateinit var mockCollectionReference: CollectionReference
    @Mock
    private lateinit var mockQuery: Query
    @Mock
    private lateinit var mockTaskQuerySnapshot: Task<QuerySnapshot>
    @Mock
    private lateinit var mockQuerySnapshot: QuerySnapshot
    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot
    @Mock
    private lateinit var mockTaskDocumentReference: Task<DocumentReference>
    @Mock
    private lateinit var mockDocumentReference: DocumentReference

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository()

        // Inject mock Firestore
        val dbField = AuthRepository::class.java.getDeclaredField("db")
        dbField.isAccessible = true
        dbField.set(authRepository, mockFirestore)

        // Mock common Firestore call chain
        `when`(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference)
        `when`(mockCollectionReference.whereEqualTo(anyString(), any())).thenReturn(mockQuery)
        `when`(mockQuery.limit(anyLong())).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(mockTaskQuerySnapshot)
        `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
        `when`(mockTaskQuerySnapshot.isComplete).thenReturn(true)
        `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
        `when`(mockTaskQuerySnapshot.exception).thenReturn(null)

        `when`(mockCollectionReference.add(any())).thenReturn(mockTaskDocumentReference)
        `when`(mockTaskDocumentReference.isSuccessful).thenReturn(true)
        `when`(mockTaskDocumentReference.isComplete).thenReturn(true)
        `when`(mockTaskDocumentReference.result).thenReturn(mockDocumentReference)
        `when`(mockTaskDocumentReference.exception).thenReturn(null)

        // Mock App.retrofitService, though not used in the Firestore methods, it's part of the AuthRepository constructor
        val retrofitServiceField = App.Companion::class.java.getDeclaredField("retrofitService")
        retrofitServiceField.isAccessible = true
        retrofitServiceField.set(null, mockWebService)
    }

    // --- Login Tests ---

    @Test
    fun `loginWithFirestore returns success if username and password match`() = runTest {
        val username = "testuser"
        val password = "hashedpassword"
        val email = "test@example.com"
        val name = "Test User"

        // Prepare mock query snapshot for existing user
        val mockDocumentList = listOf(mockDocumentSnapshot)
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentList)
        `when`(mockDocumentSnapshot.exists()).thenReturn(true)
        `when`(mockDocumentSnapshot.getString("password")).thenReturn(password)
        `when`(mockDocumentSnapshot.getString("email")).thenReturn(email)
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(name)
        `when`(mockDocumentSnapshot.id).thenReturn("someUserId") // For hashCode calculation

        val result = authRepository.loginWithFirestore(username, password)

        assertTrue(result.isSuccess)
        val authResponse = result.getOrNull()
        assertEquals("Login successful!", authResponse?.message)
        assertEquals(username, authResponse?.user?.username)
        assertEquals(email, authResponse?.user?.email)
        assertEquals(name, authResponse?.user?.name)
    }

    @Test
    fun `loginWithFirestore returns failure if username not found`() = runTest {
        val username = "nonexistent"
        val password = "anypassword"

        `when`(mockQuerySnapshot.isEmpty).thenReturn(true) // User not found

        val result = authRepository.loginWithFirestore(username, password)

        assertTrue(result.isFailure)
        assertEquals("Username not found.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithFirestore returns failure if password mismatch`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val storedPassword = "correctpassword" // Different from `password`

        // Prepare mock query snapshot for existing user
        val mockDocumentList = listOf(mockDocumentSnapshot)
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentList)
        `when`(mockDocumentSnapshot.exists()).thenReturn(true)
        `when`(mockDocumentSnapshot.getString("password")).thenReturn(storedPassword)

        val result = authRepository.loginWithFirestore(username, password)

        assertTrue(result.isFailure)
        assertEquals("Invalid username or password.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithFirestore handles Firestore exception`() = runTest {
        val username = "testuser"
        val password = "anypassword"
        val expectedException = Exception("Firestore error")

        // Mock Firestore get() to throw an exception
        `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(false)
        `when`(mockTaskQuerySnapshot.exception).thenReturn(expectedException)

        val result = authRepository.loginWithFirestore(username, password)

        assertTrue(result.isFailure)
        assertEquals("Firestore error", result.exceptionOrNull()?.message)
    }

    // --- Register Tests ---

    @Test
    fun `registerWithFirestore returns success if registration is successful`() = runTest {
        val username = "newuser"
        val name = "New User"
        val email = "new@example.com"
        val password = "newpassword"
        val cpassword = "newpassword"

        // Mock that username and email do not exist
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true) // For both existingUsername and existingEmail checks

        val result = authRepository.registerWithFirestore(username, name, email, password, cpassword)

        assertTrue(result.isSuccess)
        val authResponse = result.getOrNull()
        assertEquals("Registration successful!", authResponse?.message)
        assertEquals(username, authResponse?.user?.username)
        assertEquals(email, authResponse?.user?.email)
        assertEquals(name, authResponse?.user?.name)
    }

    @Test
    fun `registerWithFirestore returns failure if username already exists`() = runTest {
        val username = "existinguser"
        val name = "New User"
        val email = "new@example.com"
        val password = "newpassword"
        val cpassword = "newpassword"

        // Mock that username already exists
        `when`(mockQuery.get()).thenReturn(mockTaskQuerySnapshot) // This is for the first query (username)
        `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
        `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false) // Username exists

        val result = authRepository.registerWithFirestore(username, name, email, password, cpassword)

        assertTrue(result.isFailure)
        assertEquals("Username already exists.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore returns failure if email already exists`() = runTest {
        val username = "newuser"
        val name = "New User"
        val email = "existing@example.com"
        val password = "newpassword"
        val cpassword = "newpassword"

        // Mock username does not exist
        `when`(mockQuery.get()) // First call for username check
            .thenReturn(mockTaskQuerySnapshot)
        `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
        `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true) // Username does not exist

        // Mock email already exists
        val mockExistingEmailQuerySnapshot = mock(QuerySnapshot::class.java)
        val mockExistingEmailTask = mock(Task::class.java) as Task<QuerySnapshot>
        `when`(mockExistingEmailQuerySnapshot.isEmpty).thenReturn(false) // Email exists
        `when`(mockExistingEmailTask.isSuccessful).thenReturn(true)
        `when`(mockExistingEmailTask.isComplete).thenReturn(true)
        `when`(mockExistingEmailTask.result).thenReturn(mockExistingEmailQuerySnapshot)
        `when`(mockFirestore.collection(anyString()).whereEqualTo("email", email).limit(anyLong()).get()).thenReturn(mockExistingEmailTask)

        val result = authRepository.registerWithFirestore(username, name, email, password, cpassword)

        assertTrue(result.isFailure)
        assertEquals("Email already exists.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore handles Firestore add exception`() = runTest {
        val username = "newuser"
        val name = "New User"
        val email = "new@example.com"
        val password = "newpassword"
        val cpassword = "newpassword"
        val expectedException = Exception("Firestore add error")

        // Mock that username and email do not exist
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

        // Mock add operation to fail
        `when`(mockTaskDocumentReference.isSuccessful).thenReturn(false)
        `when`(mockTaskDocumentReference.exception).thenReturn(expectedException)

        val result = authRepository.registerWithFirestore(username, name, email, password, cpassword)

        assertTrue(result.isFailure)
        assertEquals("Firestore add error", result.exceptionOrNull()?.message)
    }
}