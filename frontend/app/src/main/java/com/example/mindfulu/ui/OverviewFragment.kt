package com.example.mindfulu.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mindfulu.MoodData
import com.example.mindfulu.R
import com.example.mindfulu.databinding.FragmentOverviewBinding
import com.example.mindfulu.viewmodel.MoodViewModel
import com.google.firebase.auth.FirebaseAuth

class OverviewFragment : Fragment() {

    private lateinit var binding: FragmentOverviewBinding
    private val moodViewModel: MoodViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverviewBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        val userEmail = arguments?.getString("user_email_key") ?: auth.currentUser?.email

        if (userEmail != null) {
            moodViewModel.getAllMoods(userEmail)
        } else {
            binding.progressBarOverview.isVisible = false
            binding.tvEmptyStateOverview.isVisible = true
            binding.tvEmptyStateOverview.text = "Login to see your mood overview."
            Toast.makeText(context, "User not authenticated or email not found for overview.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupObservers() {
        moodViewModel.moodHistory.observe(viewLifecycleOwner) { moods ->
            if (moods.isNullOrEmpty()) {
                binding.tvEmptyStateOverview.isVisible = true
                binding.moodChartContainer.isVisible = false
            } else {
                binding.tvEmptyStateOverview.isVisible = false
                binding.moodChartContainer.isVisible = true
                displayMoodOverview(moods)
            }
        }

        moodViewModel.error.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, "Error loading mood overview: $error", Toast.LENGTH_LONG).show()
            binding.progressBarOverview.isVisible = false
            binding.tvEmptyStateOverview.isVisible = true
            binding.tvEmptyStateOverview.text = "Failed to load overview data."
            binding.moodChartContainer.isVisible = false
        }

        moodViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarOverview.isVisible = isLoading
            binding.moodChartContainer.isVisible = !isLoading
        }
    }

    private fun displayMoodOverview(moods: List<MoodData>) {
        binding.moodChartContainer.removeAllViews()

        val moodCounts = mutableMapOf(
            "Sad" to 0,
            "Bad" to 0,
            "Happy" to 0,
            "Lovely" to 0
        )

        for (moodEntry in moods) {
            val normalizedMood = moodEntry.mood.capitalize()
            if (moodCounts.containsKey(normalizedMood)) {
                moodCounts[normalizedMood] = moodCounts[normalizedMood]!! + 1
            }
        }

        var maxCount = 0
        moodCounts.values.forEach { count ->
            if (count > maxCount) {
                maxCount = count
            }
        }

        if (maxCount == 0) {
            binding.tvEmptyStateOverview.isVisible = true
            binding.moodChartContainer.isVisible = false
            return
        }

        val colorSad = ContextCompat.getColor(requireContext(), R.color.mood_sad)
        val colorBad = ContextCompat.getColor(requireContext(), R.color.mood_bad)
        val colorHappy = ContextCompat.getColor(requireContext(), R.color.mood_happy)
        val colorLovely = ContextCompat.getColor(requireContext(), R.color.mood_lovely)

        val moodOrder = listOf("Sad", "Bad", "Happy", "Lovely")

        val maxBarHeightDp = 200

        for (moodType in moodOrder) {
            val count = moodCounts[moodType] ?: 0

            val barHeightPx = if (maxCount > 0) {
                // [PERBAIKAN] Konversi hasil float ke Int sebelum memanggil dpToPx()
                ((count.toFloat() / maxCount * maxBarHeightDp).toInt()).dpToPx()
            } else {
                0
            }

            val barContainer = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                ).apply {
                    setMargins(8.dpToPx(), 0, 8.dpToPx(), 0)
                }
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM
            }

            val countTextView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                }
                text = count.toString()
                setTextColor(Color.WHITE)
                textSize = 14f
                isVisible = count > 0
            }
            barContainer.addView(countTextView)

            val barView = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    barHeightPx
                )
                setBackgroundColor(when (moodType) {
                    "Sad" -> colorSad
                    "Bad" -> colorBad
                    "Happy" -> colorHappy
                    "Lovely" -> colorLovely
                    else -> Color.GRAY
                })
            }
            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
            barContainer.addView(spacer)
            barContainer.addView(barView)


            val labelTextView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                    topMargin = 4.dpToPx()
                }
                text = moodType
                setTextColor(Color.WHITE)
                textSize = 12f
            }
            barContainer.addView(labelTextView)

            binding.moodChartContainer.addView(barContainer)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density + 0.5f).toInt()
    }
}