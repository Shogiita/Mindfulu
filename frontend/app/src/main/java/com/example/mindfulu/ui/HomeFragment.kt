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
import com.example.mindfulu.data.SuggestionResponse
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
        } ?: run {
        }


        // [DIUBAH] Ambil data mood yang terpilih (dari MoodInsertActivity atau pengalihan langsung)
        activity?.intent?.getStringExtra("selected_mood")?.let { mood ->
            updateMoodImage(mood)
        } ?: run {
            // [BARU] Jika tidak ada mood yang dipilih, set gambar default atau sembunyikan
            binding.imageView.setImageResource(R.drawable.happy) // Default happy face
        }
    }

    private fun setupObservers() {
        suggestionViewModel.suggestions.observe(viewLifecycleOwner) { suggestionResponse ->
            displaySuggestions(suggestionResponse)
        }
    }

    private fun displaySuggestions(suggestions: SuggestionResponse) {
        with(suggestions.suggestions.saranMusik) {
            binding.tvMusicTitle.text = judul
            binding.tvMusicArtist.text = artis
            binding.tvMusicReason.text = alasan
            binding.btnPlayMusic.setOnClickListener {
                linkVideo?.let { url ->
                    val bundle = Bundle().apply {
                        putString("video_url_key", url)
                    }
                    findNavController().navigate(R.id.action_homeFragment_to_youtubeFragment, bundle)
                }
            }
        }

        val activities = suggestions.suggestions.saranKegiatan
        binding.rvActivities.layoutManager = LinearLayoutManager(context)
        binding.rvActivities.adapter = ActivitySuggestionsAdapter(activities)
    }

    private fun updateMoodImage(mood: String) {
        val moodDrawable = when (mood.lowercase()) {
            "sad" -> R.drawable.sad
            "bad" -> R.drawable.ok
            "happy" -> R.drawable.happy
            "lovely" -> R.drawable.lovely
            else -> R.drawable.happy
        }
        binding.imageView.setImageResource(moodDrawable)
    }
}