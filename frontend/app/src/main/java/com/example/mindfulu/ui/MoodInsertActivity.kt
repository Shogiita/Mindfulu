package com.example.mindfulu

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import java.util.Calendar // Import Calendar

class MoodInsertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodInsertBinding
    private val moodViewModel: MoodViewModel by viewModels()
    private val suggestionViewModel: SuggestionViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    private var moodSelected: String = ""
    private var reasonText: String = ""
    private var currentUserEmail: String? = null

    // [BARU] Flag untuk menandakan apakah mood sudah disubmit hari ini
    private var hasSubmittedMoodToday: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodInsertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        currentUserEmail = intent.getStringExtra("user_email_key")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // [BARU] Nonaktifkan UI input mood secara default hingga status mood hari ini diperiksa
        setMoodInputEnabled(false)
        binding.progressBar.isVisible = true // Tampilkan progress bar saat memeriksa mood

        setupObservers() // Setup observers terlebih dahulu

        // [BARU] Panggil fungsi untuk memeriksa apakah mood sudah disubmit hari ini
        currentUserEmail?.let { email ->
            moodViewModel.getAllMoods(email) // Ambil semua riwayat mood
        } ?: run {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show()
            navigateToLoginOrHome(null) // Arahkan ke login/home jika email null
            finish()
        }

        setupClickListeners() // Setup click listeners setelah observers
    }

    private fun setupObservers() {
        // Observer untuk hasil post mood
        moodViewModel.moodResult.observe(this) { moodResponse ->
            Toast.makeText(this, "Mood saved: ${moodResponse.message}", Toast.LENGTH_SHORT).show()
            hasSubmittedMoodToday = true // Set flag setelah berhasil submit
            // Langsung panggil suggestion view model
            if (moodSelected.isNotEmpty() && reasonText.isNotEmpty()) {
                suggestionViewModel.getSuggestions(moodSelected, reasonText)
            }
        }

        // [BARU] Observer khusus untuk memeriksa riwayat mood saat aktivitas dibuat
        moodViewModel.moodHistory.observe(this) { moods ->
            // Pastikan observer ini hanya dijalankan sekali untuk pemeriksaan awal
            // atau jika data history mood di-refresh oleh ViewModel
            if (!hasSubmittedMoodToday && moods != null) { // Hanya jalankan jika belum disubmit hari ini dan data tidak null
                val today = System.currentTimeMillis()
                val moodForToday = moods.find { moodData ->
                    App.isSameDay(moodData.date, today)
                }

                if (moodForToday != null) {
                    hasSubmittedMoodToday = true // Tandai bahwa mood sudah ada hari ini
                    Toast.makeText(this, "Anda sudah mengisi mood hari ini.", Toast.LENGTH_SHORT).show()
                    navigateToHomeOrSuggestions(moodForToday.mood, moodForToday.reason) // Arahkan ke HomeActivity dengan data mood hari ini
                    finish()
                } else {
                    // Jika belum ada mood untuk hari ini, aktifkan UI input
                    setMoodInputEnabled(true)
                    binding.progressBar.isVisible = false
                }
            }
        }


        // Observer untuk hasil suggestion
        suggestionViewModel.suggestions.observe(this) { suggestionResponse: SuggestionResponse ->
            // [DIUBAH] Gunakan navigateToHomeOrSuggestions untuk navigasi setelah mendapatkan saran
            navigateToHomeOrSuggestions(suggestionResponse.suggestions.saranMusik.judul, suggestionResponse.suggestions.saranMusik.alasan, suggestionResponse)
            finish()
        }

        // Observer untuk error dari MoodViewModel
        moodViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            binding.progressBar.isVisible = false
            // Aktifkan kembali input jika terjadi error saat post mood
            setMoodInputEnabled(true)
        }

        // Observer untuk error dari SuggestionViewModel
        suggestionViewModel.error.observe(this) { error ->
            Toast.makeText(this, "Suggestion Error: $error", Toast.LENGTH_LONG).show()
            // [DIUBAH] Jika gagal mendapatkan saran, tetap navigasi ke halaman utama
            navigateToHomeOrSuggestions()
        }

        // Observer untuk status loading dari MoodViewModel
        moodViewModel.isLoading.observe(this) { isLoading ->
            // [DIUBAH] Jangan ubah enable state tombol submit berdasarkan isLoading dari getAllMoods
            // Hanya kontrol visibility progressBar
            binding.progressBar.isVisible = isLoading
            // Tombol submit hanya di-disable saat postMood, bukan saat getAllMoods
            if (!isLoading && !hasSubmittedMoodToday) {
                binding.SubmitButton.isEnabled = true
            } else if (isLoading && !hasSubmittedMoodToday) {
                binding.SubmitButton.isEnabled = false // Disable saat postMood sedang berlangsung
            }
        }

        // Observer untuk status loading dari SuggestionViewModel
        suggestionViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.SubmitButton.isEnabled = !isLoading // Disable tombol submit saat fetching suggestion
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
            // [BARU] Cek lagi di sini untuk memastikan tidak ada double submission
            if (hasSubmittedMoodToday) {
                Toast.makeText(this, "Anda sudah mengisi mood hari ini.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentReason = binding.etReason.text.toString().trim()
            val userEmail = currentUserEmail ?: auth.currentUser?.email

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
                    this.reasonText = currentReason
                    setMoodInputEnabled(false) // Disable input saat proses submit
                    moodViewModel.postMood(moodSelected, currentReason, userEmail)
                }
            }
        }
    }

    private fun selectMood(mood: String, selectedButton: ImageButton) {
        moodSelected = mood

        binding.SadEmoji.backgroundTintList = null
        binding.BadEmoji.backgroundTintList = null
        binding.HappyEmoji.backgroundTintList = null
        binding.LovelyEmoji.backgroundTintList = null

        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
    }

    // [BARU] Fungsi navigasi ke HomeActivity yang lebih fleksibel
    private fun navigateToHomeOrSuggestions(
        mood: String? = null,
        reason: String? = null,
        suggestionResponse: SuggestionResponse? = null
    ) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("user_email_key", currentUserEmail) // Pastikan email diteruskan
            suggestionResponse?.let {
                putExtra("suggestions_data", it)
            }
            if (mood != null) {
                putExtra("selected_mood", mood) // Teruskan mood jika tersedia
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    // [BARU] Fungsi untuk navigasi kembali ke login jika terjadi error autentikasi
    private fun navigateToLoginOrHome(userEmail: String?) {
        val intent = if (userEmail.isNullOrEmpty()) {
            Intent(this, MainActivity::class.java) // Kembali ke login jika email tidak ada
        } else {
            Intent(this, HomeActivity::class.java).apply {
                putExtra("user_email_key", userEmail) // Tetap teruskan email jika ada
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}