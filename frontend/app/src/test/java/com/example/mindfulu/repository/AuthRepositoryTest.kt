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
@RunWith(MockitoJUnitRunner::class) // Gunakan runner ini untuk inisialisasi mock yang lebih baik
class AuthRepositoryTest {

    // Target yang akan diuji
    private lateinit var authRepository: AuthRepository

    // Dependensi yang akan di-mock
    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    // Objek mock pembantu
    @Mock
    private lateinit var mockCollectionReference: CollectionReference
    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    @Before
    fun setup() {
        // [PERBAIKAN KUNCI] Suntikkan mock Firestore ke repository melalui konstruktor
        authRepository = AuthRepository(mockFirestore)

        // Atur agar setiap panggilan ke collection("users") mengembalikan mock collection reference
        `when`(mockFirestore.collection("users")).thenReturn(mockCollectionReference)
    }

    // --- Tes untuk Login ---

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

        // (Act) Jalankan fungsi yang diuji
        val result = authRepository.loginWithFirestore(username, password)

        // (Assert) Pastikan hasilnya sukses dan datanya benar
        assertTrue(result.isSuccess)
        val expectedUser = UserResponse(id = userId.hashCode(), username = username, name = name, email = email)
        val expectedResponse = AuthResponse("Login successful!", expectedUser)
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `loginWithFirestore harus mengembalikan kegagalan jika username tidak ditemukan`() = runTest {
        // (Arrange) Atur skenario: Pengguna tidak ditemukan
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "nonexistent")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true) // Kunci: snapshot kosong

        // (Act) Jalankan fungsi
        val result = authRepository.loginWithFirestore("nonexistent", "password")

        // (Assert) Pastikan hasilnya gagal dengan pesan yang benar
        assertTrue(result.isFailure)
        assertEquals("Username not found.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithFirestore harus mengembalikan kegagalan jika password tidak cocok`() = runTest {
        // (Arrange) Atur skenario: Pengguna ada, tapi password salah
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "testuser")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
        `when`(mockDocumentSnapshot.getString("password")).thenReturn("correctpassword") // Password yang disimpan

        // [PERBAIKAN 2] Tambahkan mock untuk .id agar tidak terjadi NullPointerException
        `when`(mockDocumentSnapshot.id).thenReturn("anyId")

        // (Act) Jalankan fungsi dengan password yang salah
        val result = authRepository.loginWithFirestore("testuser", "wrongpassword")

        // (Assert) Pastikan hasilnya gagal dengan pesan yang benar
        assertTrue(result.isFailure)
        assertEquals("Invalid username or password.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithFirestore harus menangani exception dari Firestore`() = runTest {
        // (Arrange) Atur skenario: Operasi get() dari Firestore gagal
        val expectedException = Exception("Firestore error")
        val mockQuery = mock(Query::class.java)

        // [PERBAIKAN 1] Gunakan parameter yang benar untuk whereEqualTo
        `when`(mockCollectionReference.whereEqualTo("username", "user")).thenReturn(mockQuery)

        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forException(expectedException)) // Kunci: kembalikan Task yang gagal

        // (Act) Jalankan fungsi
        val result = authRepository.loginWithFirestore("user", "pass")

        // (Assert) Pastikan hasilnya gagal dan exception-nya sama
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    // --- Tes untuk Register ---

    @Test
    fun `registerWithFirestore harus berhasil jika username dan email belum ada`() = runTest {
        // (Arrange) Atur skenario: Username dan email belum terdaftar
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)
        val mockDocumentReference = mock(DocumentReference::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "newuser")).thenReturn(mockQuery)
        `when`(mockCollectionReference.whereEqualTo("email", "new@example.com")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true) // Kunci: username dan email belum ada
        `when`(mockCollectionReference.add(any())).thenReturn(Tasks.forResult(mockDocumentReference))

        // (Act) Jalankan fungsi
        val result = authRepository.registerWithFirestore("newuser", "New User", "new@example.com", "pass", "pass")

        // (Assert) Pastikan hasilnya sukses
        assertTrue(result.isSuccess)
        assertEquals("Registration successful!", result.getOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore harus gagal jika username sudah ada`() = runTest {
        // (Arrange) Atur skenario: Username sudah terdaftar
        val mockQuery = mock(Query::class.java)
        val nonEmptySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "existinguser")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(nonEmptySnapshot))
        `when`(nonEmptySnapshot.isEmpty).thenReturn(false) // Kunci: username ada

        // (Act) Jalankan fungsi
        val result = authRepository.registerWithFirestore("existinguser", "User", "email@example.com", "pass", "pass")

        // (Assert) Pastikan hasilnya gagal dengan pesan yang benar
        assertTrue(result.isFailure)
        assertEquals("Username already exists.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore harus gagal jika email sudah ada`() = runTest {
        val email = "existing@example.com"

        // (Arrange) Atur skenario: Username belum ada, tapi email sudah ada
        val mockUsernameQuery = mock(Query::class.java)
        val mockEmailQuery = mock(Query::class.java)
        val emptySnapshot = mock(QuerySnapshot::class.java)
        val nonEmptySnapshot = mock(QuerySnapshot::class.java)

        // Skenario untuk pengecekan username (return kosong)
        `when`(mockCollectionReference.whereEqualTo("username", "newuser")).thenReturn(mockUsernameQuery)
        `when`(mockUsernameQuery.limit(1)).thenReturn(mockUsernameQuery)
        `when`(mockUsernameQuery.get()).thenReturn(Tasks.forResult(emptySnapshot))
        `when`(emptySnapshot.isEmpty).thenReturn(true)

        // Skenario untuk pengecekan email (return tidak kosong)
        `when`(mockCollectionReference.whereEqualTo("email", email)).thenReturn(mockEmailQuery)
        `when`(mockEmailQuery.limit(1)).thenReturn(mockEmailQuery)
        `when`(mockEmailQuery.get()).thenReturn(Tasks.forResult(nonEmptySnapshot))
        `when`(nonEmptySnapshot.isEmpty).thenReturn(false) // Kunci: email ada

        // (Act) Jalankan fungsi
        val result = authRepository.registerWithFirestore("newuser", "User", email, "pass", "pass")

        // (Assert) Pastikan hasilnya gagal dengan pesan yang benar
        assertTrue(result.isFailure)
        assertEquals("Email already exists.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerWithFirestore harus menangani exception saat menambahkan dokumen`() = runTest {
        // (Arrange) Atur skenario: Pengecekan username & email lolos, tapi operasi 'add' gagal
        val expectedException = Exception("Firestore add error")
        val mockQuery = mock(Query::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)

        `when`(mockCollectionReference.whereEqualTo("username", "user")).thenReturn(mockQuery)
        `when`(mockCollectionReference.whereEqualTo("email", "email@example.com")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)
        `when`(mockCollectionReference.add(any())).thenReturn(Tasks.forException(expectedException)) // Kunci: 'add' gagal

        // (Act) Jalankan fungsi
        val result = authRepository.registerWithFirestore("user", "Name", "email@example.com", "pass", "pass")

        // (Assert) Pastikan hasilnya gagal dengan exception yang sama
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
    }
}
