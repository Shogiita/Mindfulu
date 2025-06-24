package com.example.mindfulu.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindfulu.R
import com.example.mindfulu.adapter.ActivitySuggestionsAdapter
import com.example.mindfulu.data.SuggestionResponse // Perbaikan: Import dengan package lengkap
import com.example.mindfulu.databinding.FragmentHomeBinding
import com.example.mindfulu.viewmodel.SuggestionViewModel

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val suggestionViewModel: SuggestionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        // Ambil data saran dari intent
        activity?.intent?.getParcelableExtra<SuggestionResponse>("suggestions_data")?.let { suggestions ->
            displaySuggestions(suggestions)
        }

        // TAMBAHKAN BLOK INI UNTUK MENGAMBIL DATA MOOD
        activity?.intent?.getStringExtra("selected_mood")?.let { mood ->
            updateMoodImage(mood)
        }
    }

    private fun setupObservers() {
        // Corrected: Typo in parameter name from 'SuggestionReponse' to 'suggestionResponse'
        suggestionViewModel.suggestions.observe(viewLifecycleOwner) { suggestionResponse ->
            displaySuggestions(suggestionResponse)
        }
    }

    private fun displaySuggestions(suggestions: SuggestionResponse) {
        // Display music suggestion
        with(suggestions.suggestions.saranMusik) {
            binding.tvMusicTitle.text = judul
            binding.tvMusicArtist.text = artis
            binding.tvMusicReason.text = alasan
            binding.btnPlayMusic.setOnClickListener {
                // Open YouTube link if available
                linkVideo?.let { url ->
                    val bundle = Bundle().apply {
                        putString("video_url_key", url)
                    }
                    findNavController().navigate(R.id.action_homeFragment_to_youtubeFragment, bundle)
                }
            }
        }

        // Display activity suggestions
        val activities = suggestions.suggestions.saranKegiatan
        binding.rvActivities.layoutManager = LinearLayoutManager(context)
        binding.rvActivities.adapter = ActivitySuggestionsAdapter(activities)
    }

    // TAMBAHKAN FUNGSI BARU INI UNTUK MENGUBAH GAMBAR
    private fun updateMoodImage(mood: String) {
        val moodDrawable = when (mood.lowercase()) {
            "sad" -> R.drawable.sad
            "bad" -> R.drawable.ok // Sesuai dengan layout activity_mood_insert.xml
            "happy" -> R.drawable.happy
            "lovely" -> R.drawable.lovely
            else -> R.drawable.happy // Default jika mood tidak dikenali
        }
        binding.imageView.setImageResource(moodDrawable)
    }
}