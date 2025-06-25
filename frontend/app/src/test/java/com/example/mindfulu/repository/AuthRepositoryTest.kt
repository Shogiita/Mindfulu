package com.example.mindfulu.repository

import com.example.mindfulu.data.AuthResponse
import com.example.mindfulu.data.UserResponse
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockCollectionReference: CollectionReference
    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    @Before
    fun setup() {
        authRepository = AuthRepository(mockFirestore)

        `when`(mockFirestore.collection("users")).thenReturn(mockCollectionReference)
    }

    @Test
    fun `loginWithFirestore harus mengembalikan sukses jika username dan password cocok`() = runTest {
        val username = "testuser"
        val password = "hashedpassword"
        val email = "test@example.com"
        val name = "Test User"
        val userId = "user123"

        // (Arrange) Atur skenario: Pengguna ada dan password cocok
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", username)).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
        `when`(mockDocumentSnapshot.getString("password")).thenReturn(password)
        `when`(mockDocumentSnapshot.getString("email")).thenReturn(email)
        `when`(mockDocumentSnapshot.getString("name")).thenReturn(name)
        `when`(mockDocumentSnapshot.id).thenReturn(userId)

        val result = authRepository.loginWithFirestore(username, password)

        assertTrue(result.isSuccess)
        val expectedUser = UserResponse(id = userId.hashCode(), username = username, name = name, email = email)
        val expectedResponse = AuthResponse("Login successful!", expectedUser)
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `loginWithFirestore harus mengembalikan kegagalan jika username tidak ditemukan`() = runTest {
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "nonexistent")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

        val result = authRepository.loginWithFirestore("nonexistent", "password")

        assertTrue(result.isFailure)
        assertEquals("Username not found.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithFirestore harus mengembalikan kegagalan jika password tidak cocok`() = runTest {
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "testuser")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
        `when`(mockDocumentSnapshot.getString("password")).thenReturn("correctpassword")

        `when`(mockDocumentSnapshot.id).thenReturn("anyId")

        val result = authRepository.loginWithFirestore("testuser", "wrongpassword")

        assertTrue(result.isFailure)
        assertEquals("Invalid username or password.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithFirestore harus menangani exception dari Firestore`() = runTest {
        val expectedException = Exception("Firestore error")
        val mockQuery = mock(Query::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "user")).thenReturn(mockQuery)

        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forException(expectedException))

        val result = authRepository.loginWithFirestore("user", "pass")

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `registerWithFirestore harus berhasil jika username dan email belum ada`() = runTest {
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)
        val mockDocumentReference = mock(DocumentReference::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "newuser")).thenReturn(mockQuery)
        `when`(mockCollectionReference.whereEqualTo("email", "new@example.com")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)
        `when`(mockCollectionReference.add(any())).thenReturn(Tasks.forResult(mockDocumentReference))

        val result = authRepository.registerWithFirestore("newuser", "New User", "new@example.com", "pass", "pass")

        assertTrue(result.isSuccess)
        assertEquals("Registration successful!", result.getOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore harus gagal jika username sudah ada`() = runTest {
        val mockQuery = mock(Query::class.java)
        val nonEmptySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "existinguser")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(nonEmptySnapshot))
        `when`(nonEmptySnapshot.isEmpty).thenReturn(false)

        val result = authRepository.registerWithFirestore("existinguser", "User", "email@example.com", "pass", "pass")

        assertTrue(result.isFailure)
        assertEquals("Username already exists.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore harus gagal jika email sudah ada`() = runTest {
        val email = "existing@example.com"

        val mockUsernameQuery = mock(Query::class.java)
        val mockEmailQuery = mock(Query::class.java)
        val emptySnapshot = mock(QuerySnapshot::class.java)
        val nonEmptySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "newuser")).thenReturn(mockUsernameQuery)
        `when`(mockUsernameQuery.limit(1)).thenReturn(mockUsernameQuery)
        `when`(mockUsernameQuery.get()).thenReturn(Tasks.forResult(emptySnapshot))
        `when`(emptySnapshot.isEmpty).thenReturn(true)

        `when`(mockCollectionReference.whereEqualTo("email", email)).thenReturn(mockEmailQuery)
        `when`(mockEmailQuery.limit(1)).thenReturn(mockEmailQuery)
        `when`(mockEmailQuery.get()).thenReturn(Tasks.forResult(nonEmptySnapshot))
        `when`(nonEmptySnapshot.isEmpty).thenReturn(false)

        val result = authRepository.registerWithFirestore("newuser", "User", email, "pass", "pass")

        assertTrue(result.isFailure)
        assertEquals("Email already exists.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore harus menangani exception saat menambahkan dokumen`() = runTest {
        val expectedException = Exception("Firestore add error")
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "user")).thenReturn(mockQuery)
        `when`(mockCollectionReference.whereEqualTo("email", "email@example.com")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)
        `when`(mockCollectionReference.add(any())).thenReturn(Tasks.forException(expectedException)) // Kunci: 'add' gagal

        val result = authRepository.registerWithFirestore("user", "Name", "email@example.com", "pass", "pass")

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }
}
