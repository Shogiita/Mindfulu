package com.example.mindfulu.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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

        activity?.intent?.getParcelableExtra<SuggestionResponse>("suggestions_data")?.let { suggestions ->
            displaySuggestions(suggestions)
        } ?: run {
            // [NEW] Handle case where no suggestion data is available
            Log.d("HomeFragment", "No suggestion data found in intent.")
            binding.tvMusicTitle.text = "No Music Suggestion"
            binding.tvMusicArtist.text = "Submit your mood to get one!"
            binding.tvMusicReason.text = ""
            binding.rvActivities.adapter = ActivitySuggestionsAdapter(emptyList())
        }

        activity?.intent?.getStringExtra("selected_mood")?.let { mood ->
            updateMoodImage(mood)
        } ?: run {
            binding.imageView.setImageResource(R.drawable.happy) // Default image
        }
    }

    private fun setupObservers() {
        suggestionViewModel.suggestions.observe(viewLifecycleOwner) { suggestionResponse ->
            displaySuggestions(suggestionResponse!!)
        }
    }

    private fun displaySuggestions(suggestions: SuggestionResponse) {
        val music = suggestions.suggestions.saranMusik
        binding.tvMusicTitle.text = music.judul
        binding.tvMusicArtist.text = music.artis
        binding.tvMusicReason.text = music.alasan

        binding.btnPlayMusic.setOnClickListener {
            music.linkVideo?.let { url ->
                val bundle = bundleOf("video_url_key" to url)
                findNavController().navigate(R.id.action_homeFragment_to_youtubeFragment, bundle)
            }
        }

        binding.rvActivities.layoutManager = LinearLayoutManager(context)
        binding.rvActivities.adapter = ActivitySuggestionsAdapter(suggestions.suggestions.saranKegiatan)
    }

    private fun updateMoodImage(mood: String) {
        val moodDrawable = when (mood.lowercase()) {
            "sad" -> R.drawable.sad
            "bad" -> R.drawable.ok
            "happy" -> R.drawable.happy
            "lovely" -> R.drawable.lovely
            else -> R.drawable.happy // Default to happy if mood is unrecognized
        }
        binding.imageView.setImageResource(moodDrawable)
    }
}
