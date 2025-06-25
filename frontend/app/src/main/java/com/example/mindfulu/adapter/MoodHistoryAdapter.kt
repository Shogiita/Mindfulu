package com.example.mindfulu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mindfulu.MoodData
import com.example.mindfulu.databinding.ItemMoodHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class MoodHistoryAdapter(
    private var moods: List<MoodData>
) : RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    fun updateMoods(newMoods: List<MoodData>) {
        moods = newMoods
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position])
    }

    override fun getItemCount(): Int = moods.size

    class MoodViewHolder(
        private val binding: ItemMoodHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: MoodData) {
            binding.tvMood.text = mood.mood
            binding.tvReason.text = mood.reason

            val date = Date(mood.date)
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            binding.tvDate.text = formatter.format(date)

            binding.ivMoodEmoji.text = when(mood.mood.toLowerCase()) {
                "sad" -> "😢"
                "bad" -> "😞"
                "happy" -> "😊"
                "lovely" -> "😍"
                else -> "😐"
            }
        }
    }
}