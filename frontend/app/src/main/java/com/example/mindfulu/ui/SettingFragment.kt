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
import coil.load
import coil.transform.CircleCropTransformation
import com.example.mindfulu.MockDB
import com.example.mindfulu.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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
    private lateinit var googleSignInClient: GoogleSignInClient

    // Flag untuk mendeteksi jenis login
    private var isGoogleSignIn = false
    private var isManualLogin = false

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser

        if (user != null) {
            // Cek apakah ini Google Sign-In
            checkLoginMethod(user)
            fetchAndDisplayUserData(user)
        } else {
            // Cek apakah ada data manual login dari arguments
            val emailFromArgs = arguments?.getString("user_email_key")
            val nameFromArgs = arguments?.getString("user_name_key")

            if (!emailFromArgs.isNullOrEmpty()) {
                isManualLogin = true
                isGoogleSignIn = false
                displayManualLoginData(nameFromArgs, emailFromArgs)
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

        // Initialize Google Sign-In Client
        setupGoogleSignInClient()

        nameTextView = view.findViewById(R.id.Name)
        emailTextView = view.findViewById(R.id.Email)
        profileImageView = view.findViewById(R.id.ProfileImage)
        logoutButton = view.findViewById(R.id.LogoutButton)

        logoutButton.isEnabled = false

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun setupGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Pastikan ada di strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun checkLoginMethod(user: FirebaseUser) {
        // Reset flags
        isGoogleSignIn = false
        isManualLogin = false

        // Cek provider yang digunakan untuk login
        for (profile in user.providerData) {
            when (profile.providerId) {
                GoogleAuthProvider.PROVIDER_ID -> {
                    isGoogleSignIn = true
                    Log.d(TAG, "User logged in with Google")
                }
                "password" -> {
                    isManualLogin = true
                    Log.d(TAG, "User logged in with email/password")
                }
            }
        }

        // Jika tidak ada provider yang terdeteksi, cek dari arguments
        if (!isGoogleSignIn && !isManualLogin) {
            val emailFromArgs = arguments?.getString("user_email_key")
            if (!emailFromArgs.isNullOrEmpty()) {
                isManualLogin = true
            }
        }
    }

    private fun displayManualLoginData(name: String?, email: String) {
        nameTextView.text = name ?: MockDB.name
        emailTextView.text = email
        profileImageView.setImageResource(R.drawable.baseline_account_circle_24)
        logoutButton.isEnabled = true
        Log.d(TAG, "Displaying manual login data: Email=$email")
    }

    private fun performLogout() {
        logoutButton.isEnabled = false

        when {
            isGoogleSignIn -> {
                // Logout untuk Google Sign-In
                Log.d(TAG, "Performing Google Sign-In logout")

                // 1. Sign out dari Google Sign-In Client
                googleSignInClient.signOut().addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Google Sign-In client signed out successfully")

                        // 2. Sign out dari Firebase Auth
                        auth.signOut()
                        Log.d(TAG, "Firebase Auth signed out successfully")

                        // 3. Clear any cached data
                        clearUserData()

                        // 4. Navigate to login
                        navigateToLogin()
                    } else {
                        Log.e(TAG, "Failed to sign out from Google Sign-In client", task.exception)
                        // Tetap coba logout dari Firebase
                        auth.signOut()
                        navigateToLogin()
                    }
                }
            }

            isManualLogin -> {
                // Logout untuk manual login
                Log.d(TAG, "Performing manual login logout")

                if (auth.currentUser != null) {
                    // Jika ada Firebase session, sign out
                    auth.signOut()
                }

                clearUserData()
                navigateToLogin()
            }

            else -> {
                // Fallback logout
                Log.d(TAG, "Performing fallback logout")
                auth.signOut()
                googleSignInClient.signOut()
                clearUserData()
                navigateToLogin()
            }
        }
    }

    private fun clearUserData() {
        // Clear any local data/preferences if needed
        // Misalnya SharedPreferences, cache, dll
        nameTextView.text = ""
        emailTextView.text = ""
        profileImageView.setImageResource(R.drawable.baseline_account_circle_24)
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
                    nameTextView.text = name ?: MockDB.name
                    emailTextView.text = email ?: MockDB.email
                    Log.d(TAG, "Successfully fetched user data from Firestore: Name=${name}, Email=${email}")
                } else {
                    Log.d(TAG, "No Firestore document for user: $userId. Falling back to Auth data.")
                    nameTextView.text = currentUser.displayName ?: "No Name"
                    emailTextView.text = currentUser.email ?: "No Email"
                }

                // Load profile image
                profileImageView.load(currentUser.photoUrl) {
                    placeholder(R.drawable.baseline_account_circle_24)
                    error(R.drawable.baseline_account_circle_24)
                    transformations(CircleCropTransformation())
                }

                logoutButton.isEnabled = true
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting document:", exception)
                Toast.makeText(context, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
                logoutButton.isEnabled = true
            }
    }

    private fun navigateToLogin() {
        if (activity != null && isAdded) {
            val intent = Intent(activity, MainActivity::class.java)
            // Clear all previous activities from the stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }
}