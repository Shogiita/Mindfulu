package com.example.mindfulu

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
import com.example.mindfulu.data.SuggestionResponse
import com.example.mindfulu.databinding.ActivityMoodInsertBinding
import com.example.mindfulu.ui.HomeActivity
import com.example.mindfulu.ui.MainActivity
import com.example.mindfulu.viewmodel.MoodViewModel
import com.example.mindfulu.viewmodel.SuggestionViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class MoodInsertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodInsertBinding
    private val moodViewModel: MoodViewModel by viewModels()
    private val suggestionViewModel: SuggestionViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    private var moodSelected: String = ""
    private var reasonText: String = ""
    private var currentUserEmail: String? = null
    private var currentUserName: String? = null

    private var hasSubmittedMoodToday: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodInsertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        currentUserEmail = intent.getStringExtra("user_email_key")
        currentUserName = intent.getStringExtra("user_name_key")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setMoodInputEnabled(false)
        binding.progressBar.isVisible = true

        setupObservers()

        currentUserEmail?.let { email ->
            moodViewModel.getAllMoods(email)
        } ?: run {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show()
            navigateToLogin()
            finish()
        }

        setupClickListeners()
    }

    private fun setupObservers() {
        moodViewModel.moodResult.observe(this) { moodResponse ->
            Toast.makeText(this, "Mood saved: ${moodResponse.message}", Toast.LENGTH_SHORT).show()
            hasSubmittedMoodToday = true
            if (moodSelected.isNotEmpty() && reasonText.isNotEmpty() && currentUserEmail != null) {
                suggestionViewModel.getSuggestions(moodSelected, reasonText, currentUserEmail!!)
            }
        }

        moodViewModel.moodHistory.observe(this) { moods ->
            if (!hasSubmittedMoodToday && moods != null) {
                val today = System.currentTimeMillis()
                val moodForToday = moods.find { App.isSameDay(it.date, today) }

                if (moodForToday != null) {
                    hasSubmittedMoodToday = true
                    Toast.makeText(this, "Anda sudah mengisi mood hari ini. Memuat saran...", Toast.LENGTH_SHORT).show()
                    suggestionViewModel.loadSuggestionsForToday(moodForToday.email, moodForToday.mood, moodForToday.reason)
                } else {
                    setMoodInputEnabled(true)
                    binding.progressBar.isVisible = false
                }
            }
        }

        suggestionViewModel.suggestions.observe(this) { suggestionResponse ->
            navigateToHome(suggestionResponse)
            finish()
        }

        moodViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            binding.progressBar.isVisible = false
            setMoodInputEnabled(true)
        }

        suggestionViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Suggestion Error: $error", Toast.LENGTH_LONG).show()
            navigateToHome(null)
        }

        moodViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.isVisible = true
                binding.SubmitButton.isEnabled = false
            }
        }

        suggestionViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            if (isLoading) {
                binding.SubmitButton.isEnabled = false
            }
        }
    }

    private fun setMoodInputEnabled(enabled: Boolean) {
        binding.SadEmoji.isEnabled = enabled
        binding.BadEmoji.isEnabled = enabled
        binding.HappyEmoji.isEnabled = enabled
        binding.LovelyEmoji.isEnabled = enabled
        binding.etReason.isEnabled = enabled
        binding.SubmitButton.isEnabled = enabled
    }

    private fun setupClickListeners() {
        binding.SadEmoji.setOnClickListener { selectMood("Sad", it as ImageButton) }
        binding.BadEmoji.setOnClickListener { selectMood("Bad", it as ImageButton) }
        binding.HappyEmoji.setOnClickListener { selectMood("Happy", it as ImageButton) }
        binding.LovelyEmoji.setOnClickListener { selectMood("Lovely", it as ImageButton) }

        binding.SubmitButton.setOnClickListener {
            if (hasSubmittedMoodToday) {
                Toast.makeText(this, "Anda sudah mengisi mood hari ini.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val currentReason = binding.etReason.text.toString().trim()
            val userEmail = currentUserEmail ?: auth.currentUser?.email

            when {
                moodSelected.isEmpty() -> Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
                currentReason.isEmpty() -> Toast.makeText(this, "Please tell us your story", Toast.LENGTH_SHORT).show()
                userEmail.isNullOrEmpty() -> Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show()
                else -> {
                    this.reasonText = currentReason
                    setMoodInputEnabled(false)
                    moodViewModel.postMood(moodSelected, currentReason, userEmail)
                }
            }
        }
    }

    private fun selectMood(mood: String, selectedButton: ImageButton) {
        moodSelected = mood
        val buttons = listOf(binding.SadEmoji, binding.BadEmoji, binding.HappyEmoji, binding.LovelyEmoji)
        buttons.forEach { it.backgroundTintList = null }
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
    }

    private fun navigateToHome(suggestionResponse: SuggestionResponse?) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("user_email_key", currentUserEmail)
            putExtra("user_name_key", currentUserName)
            suggestionResponse?.let {
                putExtra("suggestions_data", it)
            }
            if (moodSelected.isNotEmpty()) {
                putExtra("selected_mood", moodSelected)
            } else {
                suggestionResponse?.let {
                    // This part is tricky as mood is not directly in the response
                    // We'll use a placeholder logic, ideally the backend would return the original mood.
                    // For now, let's just pass what we have.
                    // A better approach would be to get the original mood from moodForToday in the history observer.
                }
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
