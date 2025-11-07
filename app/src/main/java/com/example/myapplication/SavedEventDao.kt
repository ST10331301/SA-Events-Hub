package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface SavedEventDao {
    @Insert
    suspend fun saveEvent(savedEvent: SavedEvent)

    @Delete
    suspend fun unsaveEvent(savedEvent: SavedEvent)

    @Query("SELECT e.* FROM events e INNER JOIN saved_events se ON e.id = se.eventId WHERE se.userId = :userId")
    suspend fun getSavedEvents(userId: Int): List<Event>

    @Query("SELECT COUNT(*) FROM saved_events WHERE userId = :userId AND eventId = :eventId")
    suspend fun isEventSaved(userId: Int, eventId: Int): Int
}