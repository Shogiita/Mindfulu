package com.example.mindfulu // Pastikan package name sesuai

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.mindfulu.R
import com.example.mindfulu.data.SuggestionResponse
import com.example.mindfulu.databinding.ActivityMoodInsertBinding
import com.example.mindfulu.ui.HomeActivity
import com.example.mindfulu.viewmodel.MoodViewModel
import com.example.mindfulu.viewmodel.SuggestionViewModel
import com.google.firebase.auth.FirebaseAuth

class MoodInsertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodInsertBinding
    private val moodViewModel: MoodViewModel by viewModels()
    private val suggestionViewModel: SuggestionViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    private var moodSelected: String = ""
    private var reasonText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodInsertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observer untuk hasil post mood
        moodViewModel.moodResult.observe(this) { moodResponse ->
            Toast.makeText(this, "Mood saved: ${moodResponse.message}", Toast.LENGTH_SHORT).show()
            // Setelah mood berhasil disimpan, panggil suggestion view model
            if (moodSelected.isNotEmpty() && reasonText.isNotEmpty()) {
                // Panggil fungsi dengan nama yang sudah diperbaiki
                suggestionViewModel.getSuggestions(moodSelected, reasonText)
            }
        }

        // Observer untuk hasil suggestion
        suggestionViewModel.suggestions.observe(this) { suggestionResponse: SuggestionResponse ->
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("suggestions_data", suggestionResponse)
                // PASTIKAN BARIS INI DITAMBAHKAN:
                putExtra("selected_mood", moodSelected)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // Observer untuk error dari MoodViewModel
        moodViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            binding.progressBar.isVisible = false
            binding.SubmitButton.isEnabled = true
        }

        // Observer untuk error dari SuggestionViewModel
        suggestionViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Suggestion Error: $error", Toast.LENGTH_LONG).show()
            // Jika gagal mendapatkan saran, tetap navigasi ke halaman utama
            navigateToHome()
        }

        // Observer untuk status loading dari MoodViewModel
        moodViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.SubmitButton.isEnabled = !isLoading
        }

        // Observer untuk status loading dari SuggestionViewModel
        suggestionViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.SubmitButton.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.SadEmoji.setOnClickListener { selectMood("Sad", it as ImageButton) }
        binding.BadEmoji.setOnClickListener { selectMood("Bad", it as ImageButton) }
        binding.HappyEmoji.setOnClickListener { selectMood("Happy", it as ImageButton) }
        binding.LovelyEmoji.setOnClickListener { selectMood("Lovely", it as ImageButton) }

        binding.SubmitButton.setOnClickListener {
            val currentReason = binding.etReason.text.toString().trim()
            val userEmail = auth.currentUser?.email

            when {
                moodSelected.isEmpty() -> {
                    Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
                }
                currentReason.isEmpty() -> {
                    Toast.makeText(this, "Please tell us your story", Toast.LENGTH_SHORT).show()
                }
                userEmail.isNullOrEmpty() -> {
                    Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    // Simpan alasan ke variabel class agar bisa diakses di observer
                    this.reasonText = currentReason
                    moodViewModel.postMood(moodSelected, currentReason, userEmail)
                }
            }
        }
    }

    private fun selectMood(mood: String, selectedButton: ImageButton) {
        moodSelected = mood

        // Reset warna latar belakang semua tombol
        binding.SadEmoji.backgroundTintList = null
        binding.BadEmoji.backgroundTintList = null
        binding.HappyEmoji.backgroundTintList = null
        binding.LovelyEmoji.backgroundTintList = null

        // Sorot tombol yang dipilih dengan warna abu-abu muda
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}