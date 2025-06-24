package com.example.mindfulu.repository

import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.UserResponse
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any // Import any from mockito-kotlin

@ExperimentalCoroutinesApi
class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository
    private val mockFirestore: FirebaseFirestore = mock()
    private val mockCollection: CollectionReference = mock()
    private val mockQuery: Query = mock()
    private val mockQuerySnapshot: QuerySnapshot = mock()
    private val mockDocumentSnapshot: DocumentSnapshot = mock()
    private val mockDocumentReference: DocumentReference = mock()

    @Before
    fun setup() {
        // Mock the Firestore calls
        `when`(mockFirestore.collection(anyString())).thenReturn(mockCollection)
        `when`(mockCollection.whereEqualTo(anyString(), anyString())).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)

        authRepository = AuthRepository()
        // Here, we would ideally inject mockFirestore into AuthRepository.
        // As AuthRepository directly calls FirebaseFirestore.getInstance(),
        // we'd need PowerMock or a wrapper for proper unit testing without modifying AuthRepository.
        // For demonstration, let's assume we can somehow influence FirebaseFirestore.getInstance().
        // A better design would be to pass FirebaseFirestore instance to AuthRepository's constructor.
    }

    // This test setup is simplified. In a real scenario, consider
    // using a Firebase Test SDK or carefully mocking static methods/singletons if not
    // injecting FirebaseFirestore through the constructor.

    @Test
    fun `loginWithFirestore returns success for valid credentials`() = runTest {
        // Mock a successful query result for existing username
        `when`(mockQuery.get()).thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

        // Mock user document data
        `when`(mockDocumentSnapshot.getString("password")).thenReturn("hashedPassword123")
        `when`(mockDocumentSnapshot.getString("email")).thenReturn("test@example.com")
        `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test User")
        `when`(mockDocumentSnapshot.id).thenReturn("userId123")

        // Call the method to test
        val result = authRepository.loginWithFirestore("testuser", "hashedPassword123")

        // Assertions
        assertTrue(result.isSuccess)
        val authResponse = result.getOrNull()
        assertEquals("Login successful!", authResponse?.message)
        assertEquals("testuser", authResponse?.user?.username)
        assertEquals("test@example.com", authResponse?.user?.email)
    }

    @Test
    fun `loginWithFirestore returns failure for invalid password`() = runTest {
        // Mock a successful query result for existing username
        `when`(mockQuery.get()).thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

        // Mock user document with wrong password
        `when`(mockDocumentSnapshot.getString("password")).thenReturn("wrongPassword")

        val result = authRepository.loginWithFirestore("testuser", "correctPassword")

        assertTrue(result.isFailure)
        assertEquals("Invalid username or password.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithFirestore returns failure for username not found`() = runTest {
        // Mock an empty query result
        `when`(mockQuery.get()).thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

        val result = authRepository.loginWithFirestore("nonexistentuser", "anypassword")

        assertTrue(result.isFailure)
        assertEquals("Username not found.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore returns success for new user`() = runTest {
        // Mock empty results for existing username and email queries
        `when`(mockQuery.get()).thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

        // Mock add document result
        `when`(mockCollection.add(any<Map<String, Any>>())).thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockDocumentReference)) // <-- Perubahan di sini

        val result = authRepository.registerWithFirestore(
            "newuser", "New User", "new@example.com", "pass123", "pass123"
        )

        assertTrue(result.isSuccess)
        val authResponse = result.getOrNull()
        assertEquals("Registration successful!", authResponse?.message)
        assertEquals("newuser", authResponse?.user?.username)
        assertEquals("new@example.example.com", authResponse?.user?.email)
    }

    @Test
    fun `registerWithFirestore returns failure if username exists`() = runTest {
        // Mock a non-empty result for existing username query
        `when`(mockCollection.whereEqualTo("username", "existinguser").limit(1).get())
            .thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false) // Username exists

        val result = authRepository.registerWithFirestore(
            "existinguser", "User", "user@example.com", "pass", "pass"
        )

        assertTrue(result.isFailure)
        assertEquals("Username already exists.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore returns failure if email exists`() = runTest {
        // Mock empty result for username check
        `when`(mockCollection.whereEqualTo("username", "newuser").limit(1).get())
            .thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

        // Mock non-empty result for email check
        `when`(mockCollection.whereEqualTo("email", "existing@example.com").limit(1).get())
            .thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false) // Email exists

        val result = authRepository.registerWithFirestore(
            "newuser", "User", "existing@example.com", "pass", "pass"
        )

        assertTrue(result.isFailure)
        assertEquals("Email already exists.", result.exceptionOrNull()?.message)
    }
}