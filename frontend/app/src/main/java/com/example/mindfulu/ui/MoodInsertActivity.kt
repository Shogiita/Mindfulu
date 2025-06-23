package com.example.mindfulu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mindfulu.databinding.ActivityMoodInsertBinding
import com.example.mindfulu.ui.HomeActivity
import com.example.mindfulu.viewmodel.MoodViewModel
import com.google.firebase.auth.FirebaseAuth

class MoodInsertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodInsertBinding
    private val moodViewModel: MoodViewModel by viewModels()
    // [BARU] Mendapatkan instance Firebase Auth
    private lateinit var auth: FirebaseAuth

    private var moodSelected: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodInsertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // [BARU] Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupMoodSelection()
        setupSubmitButton()
        setupObservers()
    }

    private fun setupMoodSelection() {
        binding.SadEmoji.setOnClickListener { selectMood("Sad", it as ImageButton) }
        binding.BadEmoji.setOnClickListener { selectMood("Bad", it as ImageButton) }
        binding.HappyEmoji.setOnClickListener { selectMood("Happy", it as ImageButton) }
        binding.LovelyEmoji.setOnClickListener { selectMood("Lovely", it as ImageButton) }
    }

    private fun selectMood(mood: String, selectedButton: ImageButton) {
        moodSelected = mood
        // Reset background tint for all buttons
        binding.SadEmoji.background = null
        binding.BadEmoji.background = null
        binding.HappyEmoji.background = null
        binding.LovelyEmoji.background = null

        // Highlight the selected button (optional, for better UX)
        //selectedButton.setBackgroundResource(R.drawable.mood_selection_border)
    }

    private fun setupSubmitButton() {
        binding.SubmitButton.setOnClickListener {
            val reason = binding.etReason.text.toString().trim()
            // [BARU] Mendapatkan email pengguna saat ini
            val userEmail = auth.currentUser?.email

            when {
                moodSelected.isEmpty() -> {
                    Toast.makeText(this, "Mood must be selected", Toast.LENGTH_SHORT).show()
                }
                reason.isEmpty() -> {
                    Toast.makeText(this, "Please tell us your story", Toast.LENGTH_SHORT).show()
                }
                userEmail == null -> {
                    // [BARU] Menangani kasus jika pengguna tidak login
                    Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    // [DIUBAH] Memanggil postMood dengan email pengguna
                    moodViewModel.postMood(moodSelected, reason, userEmail)
                }
            }
        }
    }

    private fun setupObservers() {
        // Observer untuk hasil post mood
        moodViewModel.moodResult.observe(this) { response ->
            Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            // Navigasi ke HomeActivity setelah berhasil
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Observer untuk error
        moodViewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
        }

        // Observer untuk loading state
        moodViewModel.isLoading.observe(this) { isLoading ->
            // Anda bisa menampilkan atau menyembunyikan progress bar di sini jika ada
        }
    }
}
