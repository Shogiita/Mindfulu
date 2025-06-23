package com.example.mindfulu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mindfulu.data.ActivitySuggestion
import com.example.mindfulu.databinding.ItemActivitySuggestionBinding

class ActivitySuggestionsAdapter(
    private val activities: List<ActivitySuggestion>
) : RecyclerView.Adapter<ActivitySuggestionsAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivitySuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount(): Int = activities.size

    class ActivityViewHolder(
        private val binding: ItemActivitySuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: ActivitySuggestion) {
            binding.tvActivityTitle.text = activity.kegiatan
            binding.tvActivityDescription.text = activity.deskripsi
        }
    }
}
