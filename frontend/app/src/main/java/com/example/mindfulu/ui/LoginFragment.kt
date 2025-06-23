package com.example.mindfulu.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mindfulu.viewmodel.LoginRegisterViewModel
import com.example.mindfulu.R
import com.example.mindfulu.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Import for await()

class LoginFragment : Fragment() {

    private val vm: LoginRegisterViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    // Firebase & Google Sign-In
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val db = FirebaseFirestore.getInstance()

    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        // Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }

        vm.loginResult.observe(viewLifecycleOwner) {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            navigateToHome()
        }

        vm.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }

        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarLogin.isVisible = isLoading
        }

        // Button click listeners
        binding.buttonSignIn.setOnClickListener {
            val username = binding.etUsernameLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "There's an empty field!", Toast.LENGTH_SHORT).show()
            } else {
                vm.login(username, password)
            }
        }

        binding.buttonSignInWithGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.buttonToSIgnUp.setOnClickListener {
            findNavController().navigate(R.id.action_global_registerFragment)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        binding.progressBarLogin.isVisible = true
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBarLogin.isVisible = false
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(context, "Google Sign In Success", Toast.LENGTH_SHORT).show()

                    // --- BARU DITAMBAHKAN: Simpan data user ke Firestore setelah login Google ---
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        CoroutineScope(Dispatchers.IO).launch { // Gunakan coroutine untuk operasi Firestore
                            try {
                                val userId = user.uid
                                val userEmail = user.email ?: ""
                                val userName = user.displayName ?: "" // Nama dari Google
                                val userUsername = userEmail.substringBefore("@") // Contoh username dari email

                                val userDocRef = db.collection("users").document(userId)

                                // Periksa apakah dokumen user sudah ada
                                val documentSnapshot = userDocRef.get().await()
                                if (!documentSnapshot.exists()) {
                                    // Jika belum ada, buat dokumen baru
                                    val userData = hashMapOf(
                                        "email" to userEmail,
                                        "name" to userName,
                                        "username" to userUsername,
                                        // Password TIDAK disimpan di sini untuk Google Sign-In
                                    )
                                    userDocRef.set(userData).await()
                                    Log.d(TAG, "User data saved to Firestore: $userId")
                                } else {
                                    Log.d(TAG, "User data already exists in Firestore: $userId")
                                }
                                // Setelah selesai menyimpan/memverifikasi data di Firestore, baru navigasi
                                navigateToHome()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error saving Google user data to Firestore: ${e.message}", e)
                                // Tampilkan pesan error ke user jika perlu
                                with(Dispatchers.Main) {
                                    Toast.makeText(context, "Failed to store user data: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } ?: run {
                        Log.e(TAG, "Firebase User is null after Google Sign-In success.")
                        Toast.makeText(context, "Authentication failed: User data not found.", Toast.LENGTH_SHORT).show()
                    }
                    // ---------------------------------------------------------------------

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(context, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToHome() {
        val intent = Intent(activity, MoodInsertActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}