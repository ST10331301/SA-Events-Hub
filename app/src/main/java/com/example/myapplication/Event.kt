package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long, // Store as timestamp
    val location: String,
    val category: String,
    val price: Float = 0.0f,
    val imageUrl: String = "",
    val isFeatured: Boolean = false
)