package com.example.mindfulu.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindfulu.R
import com.example.mindfulu.adapter.MoodHistoryAdapter
import com.example.mindfulu.databinding.FragmentHistoryBinding
import com.example.mindfulu.viewmodel.MoodViewModel

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private val moodViewModel: MoodViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRecyclerView()

        // Load mood history
        moodViewModel.getAllMoods()
    }

    private fun setupObservers() {
        moodViewModel.moodHistory.observe(viewLifecycleOwner) { moods ->
            if (moods.isEmpty()) {
                binding.tvEmptyState.isVisible = true
                binding.rvMoodHistory.isVisible = false
            } else {
                binding.tvEmptyState.isVisible = false
                binding.rvMoodHistory.isVisible = true
                (binding.rvMoodHistory.adapter as? MoodHistoryAdapter)?.updateMoods(moods)
            }
        }

        moodViewModel.error.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, "Error loading history: $error", Toast.LENGTH_LONG).show()
        }

        moodViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }
    }

    private fun setupRecyclerView() {
        binding.rvMoodHistory.layoutManager = LinearLayoutManager(context)
        binding.rvMoodHistory.adapter = MoodHistoryAdapter(emptyList())
    }
}
