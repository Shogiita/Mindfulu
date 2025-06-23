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
import com.example.mindfulu.adapter.MoodHistoryAdapter
import com.example.mindfulu.databinding.FragmentHistoryBinding
import com.example.mindfulu.viewmodel.MoodViewModel
import com.google.firebase.auth.FirebaseAuth

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private val moodViewModel: MoodViewModel by viewModels()
    // [BARU] Mendapatkan instance Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        // [BARU] Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRecyclerView()

        // [DIUBAH] Load mood history berdasarkan email pengguna yang sedang login
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.email != null) {
            // Jika pengguna login dan memiliki email, panggil fungsi dengan email tersebut
            moodViewModel.getAllMoods(currentUser.email!!)
        } else {
            // Tangani kasus jika pengguna tidak login atau tidak memiliki email
            binding.progressBar.isVisible = false
            binding.tvEmptyState.isVisible = true
            binding.tvEmptyState.text = "You need to be logged in to see history."
            Toast.makeText(context, "User not authenticated.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupObservers() {
        moodViewModel.moodHistory.observe(viewLifecycleOwner) { moods ->
            if (moods.isEmpty()) {
                binding.tvEmptyState.isVisible = true
                binding.tvEmptyState.text = "No mood history found."
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