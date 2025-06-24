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
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var logoutButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        // [DIUBAH] Jika user Firebase null, coba ambil email dari arguments
        if (user != null) {
            fetchAndDisplayUserData(user)
        } else {
            val emailFromArgs = arguments?.getString("user_email_key")
            val nameFromArgs = arguments?.getString("user_name_key") // Jika Anda juga meneruskan nama

            if (!emailFromArgs.isNullOrEmpty()) {
                // Tampilkan data dari arguments jika tidak ada user Firebase
                nameTextView.text = nameFromArgs ?: "User Name"
                emailTextView.text = emailFromArgs
                profileImageView.setImageResource(R.drawable.baseline_account_circle_24) // Placeholder default
                logoutButton.isEnabled = true
                Log.d(TAG, "Displaying user data from arguments: Email=$emailFromArgs")
            } else {
                Log.w(TAG, "No user is currently signed in. Navigating to login.")
                navigateToLogin()
            }
        }
    }

    private val TAG = "SettingFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        nameTextView = view.findViewById(R.id.Name)
        emailTextView = view.findViewById(R.id.Email)
        profileImageView = view.findViewById(R.id.ProfileImage)
        logoutButton = view.findViewById(R.id.LogoutButton)

        logoutButton.isEnabled = false

        logoutButton.setOnClickListener {
            // [DIUBAH] Untuk logout manual, kita akan langsung navigasi ke login tanpa signOut Firebase
            // Jika Anda ingin mendukung logout Firebase untuk akun Google, Anda dapat menambahkan logika di sini.
            // Saat ini, signOut() akan menghapus sesi Firebase Auth.
            if (auth.currentUser != null) {
                signOut() // Logout dari Firebase jika user login via Google
            } else {
                navigateToLogin() // Langsung navigasi jika login manual
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
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
                    val name = document.getString("name")
                    val email = document.getString("email")
                    nameTextView.text = name ?: "No Name"
                    emailTextView.text = email ?: "No Email"
                    Log.d(TAG, "Successfully fetched user data from Firestore: Name=${name}, Email=${email}")
                } else {
                    Log.d(TAG, "No Firestore document for user: $userId. Falling back to Auth data.")
                    nameTextView.text = currentUser.displayName ?: "No Name"
                    emailTextView.text = currentUser.email ?: "No Email"
                }

                Glide.with(requireContext())
                    .load(currentUser.photoUrl)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .circleCrop()
                    .into(profileImageView)

                logoutButton.isEnabled = true
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting document:", exception)
                Toast.makeText(context, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
                logoutButton.isEnabled = true
            }
    }

    private fun signOut() {
        logoutButton.isEnabled = false
        auth.signOut()
    }

    private fun navigateToLogin() {
        if (activity != null && isAdded) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }
}