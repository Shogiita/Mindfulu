package com.example.mindfulu.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.mindfulu.R
import com.example.mindfulu.databinding.ActivityMoodInsertBinding
import com.example.mindfulu.viewmodel.MoodViewModel
import com.example.mindfulu.viewmodel.SuggestionViewModel
import com.example.mindfulu.data.SuggestionResponse

class MoodInsertActivity : AppCompatActivity() {
    private var moodSelected: String = ""
    private var reasonText: String = ""

    private lateinit var binding: ActivityMoodInsertBinding
    private val moodViewModel: MoodViewModel by viewModels()
    private val suggestionViewModel: SuggestionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMoodInsertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        moodViewModel.moodResult.observe(this) { moodResponse ->
            Toast.makeText(this, "Mood saved: ${moodResponse.message}", Toast.LENGTH_SHORT).show()

            if (moodSelected.isNotEmpty() && reasonText.isNotEmpty()) {
                suggestionViewModel.getSuggestions(moodSelected, reasonText)
            }
        }

        suggestionViewModel.suggestions.observe(this) { suggestionResponse: SuggestionResponse ->
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("suggestions_data", suggestionResponse)
            }
            startActivity(intent)
            finish()
        }

        moodViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            // Hide loading indicator on error
            binding.progressBar.isVisible = false
        }

        suggestionViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Suggestion Error: $error", Toast.LENGTH_LONG).show()
            // Hide loading indicator on error
            binding.progressBar.isVisible = false
            navigateToHome()
        }

        moodViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.SubmitButton.isEnabled = !isLoading
        }

        suggestionViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.SubmitButton.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.SadEmoji.setOnClickListener {
            selectMood("Sad", binding.SadEmoji)
        }

        binding.BadEmoji.setOnClickListener {
            selectMood("Bad", binding.BadEmoji)
        }

        binding.HappyEmoji.setOnClickListener {
            selectMood("Happy", binding.HappyEmoji)
        }

        binding.LovelyEmoji.setOnClickListener {
            selectMood("Lovely", binding.LovelyEmoji)
        }

        binding.SubmitButton.setOnClickListener {
            reasonText = binding.etReason.text.toString().trim()

            when {
                moodSelected.isEmpty() -> {
                    Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
                }
                reasonText.isEmpty() -> {
                    Toast.makeText(this, "Please provide a reason", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    moodViewModel.postMood(moodSelected, reasonText)
                }
            }
        }
    }

    private fun selectMood(mood: String, selectedButton: ImageButton) {
        moodSelected = mood

        // Reset all button backgrounds
        binding.SadEmoji.backgroundTintList = null
        binding.BadEmoji.backgroundTintList = null
        binding.HappyEmoji.backgroundTintList = null
        binding.LovelyEmoji.backgroundTintList = null

        // Highlight selected button
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00FF00"))

        Toast.makeText(this, "$mood selected", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}