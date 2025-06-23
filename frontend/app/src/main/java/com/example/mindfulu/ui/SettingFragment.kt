package com.example.mindfulu.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mindfulu.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SettingFragment : Fragment() {
    // Deklarasi view
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var logoutButton: Button

    // Deklarasi Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Deklarasi AuthStateListener
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            // Pengguna login, jalankan logika untuk mengambil data
            fetchAndDisplayUserData(user)
        } else {
            // Pengguna logout, arahkan ke halaman login.
            // Ini sekarang aman karena kita tahu status awal sudah terverifikasi.
            Log.w(TAG, "No user is currently signed in. Navigating to login.")
            navigateToLogin()
        }
    }

    private val TAG = "SettingFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase Auth dan Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        // Inisialisasi Views dari layout
        nameTextView = view.findViewById(R.id.Name)
        emailTextView = view.findViewById(R.id.Email)
        profileImageView = view.findViewById(R.id.ProfileImage)
        logoutButton = view.findViewById(R.id.LogoutButton)

        // --- SOLUSI: Nonaktifkan tombol logout pada awalnya ---
        // Tombol baru akan aktif setelah data pengguna berhasil dimuat.
        // Ini mencegah pengguna logout sebelum status auth sepenuhnya dikonfirmasi.
        logoutButton.isEnabled = false

        // Set listener untuk tombol logout
        logoutButton.setOnClickListener {
            signOut()
        }
    }

    override fun onStart() {
        super.onStart()
        // Tambahkan listener saat fragment dimulai
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        // Hapus listener saat fragment berhenti untuk menghindari memory leak
        if (::auth.isInitialized) {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    private fun fetchAndDisplayUserData(currentUser: FirebaseUser) {
        val userId = currentUser.uid
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Dokumen ditemukan di Firestore
                    val name = document.getString("name")
                    val email = document.getString("email")
                    nameTextView.text = name ?: "No Name"
                    emailTextView.text = email ?: "No Email"
                    Log.d(TAG, "Successfully fetched user data from Firestore: Name=${name}, Email=${email}")
                } else {
                    // Fallback: Jika data tidak ada di Firestore, ambil dari Auth object
                    Log.d(TAG, "No Firestore document for user: $userId. Falling back to Auth data.")
                    nameTextView.text = currentUser.displayName ?: "No Name"
                    emailTextView.text = currentUser.email ?: "No Email"
                }

                // Logika untuk menampilkan gambar profil
                Glide.with(requireContext())
                    .load(currentUser.photoUrl) // Akan null untuk login non-Google
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(profileImageView)

                // --- SOLUSI: Aktifkan tombol logout setelah data dimuat ---
                // Ini menandakan bahwa pemeriksaan auth awal telah selesai.
                logoutButton.isEnabled = true
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting document:", exception)
                Toast.makeText(context, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
                // Tetap aktifkan tombol logout meskipun gagal mengambil data dari Firestore,
                // karena pengguna masih dalam status login.
                logoutButton.isEnabled = true
            }
    }

    private fun signOut() {
        // Nonaktifkan tombol untuk mencegah klik ganda saat proses logout
        logoutButton.isEnabled = false
        auth.signOut()
        // Fungsi navigateToLogin() akan dipanggil secara otomatis oleh authStateListener
    }

    private fun navigateToLogin() {
        // Pastikan activity tidak null sebelum membuat Intent
        if (activity != null && isAdded) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }
}
