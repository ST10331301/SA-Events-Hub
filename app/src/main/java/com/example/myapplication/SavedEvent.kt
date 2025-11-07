package com.example.myapplication

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "saved_events",
    primaryKeys = ["userId", "eventId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SavedEvent(
    val userId: Int,
    val eventId: Int
)