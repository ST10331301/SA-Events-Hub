package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventsAdapter(
    private val onSaveClick: (Event) -> Unit,
    private val isEventSaved: (Int) -> Boolean
) : ListAdapter<Event, EventsAdapter.EventViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = com.example.myapplication.databinding.ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.eventTitle.text = event.title

            // Format date
            val date = Date(event.date)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.eventDate.text = dateFormat.format(date)

            binding.eventLocation.text = event.location
            binding.categoryChip.text = event.category
            binding.eventPrice.text = "R${event.price}"

            val saved = isEventSaved(event.id)
            binding.btnSaveEvent.text = if (saved) "Unsave" else "Save"

            // Use our custom bookmark icons
            val iconRes = if (saved) {
                R.drawable.ic_bookmark_filled
            } else {
                R.drawable.ic_bookmark_border
            }
            binding.btnSaveEvent.setIconResource(iconRes)

            binding.btnSaveEvent.setOnClickListener {
                onSaveClick(event)
            }

            // Set category chip color based on category
            val context = binding.root.context
            when (event.category.lowercase()) {
                "music" -> binding.categoryChip.setChipBackgroundColorResource(android.R.color.holo_purple)
                "sports" -> binding.categoryChip.setChipBackgroundColorResource(android.R.color.holo_green_light)
                "food" -> binding.categoryChip.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                "arts" -> binding.categoryChip.setChipBackgroundColorResource(android.R.color.holo_red_light)
                else -> binding.categoryChip.setChipBackgroundColorResource(android.R.color.holo_blue_light)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}