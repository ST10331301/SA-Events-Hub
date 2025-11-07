package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.Event

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event): Long

    @Query("SELECT * FROM events ORDER BY date ASC")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Int): Event?

    @Query("SELECT * FROM events WHERE category = :category ORDER BY date ASC")
    suspend fun getEventsByCategory(category: String): List<Event>

    @Query("SELECT * FROM events WHERE location LIKE '%' || :location || '%' ORDER BY date ASC")
    suspend fun getEventsByLocation(location: String): List<Event>

    @Query("SELECT * FROM events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY date ASC")
    suspend fun searchEvents(query: String): List<Event>

    @Query("SELECT * FROM events WHERE isFeatured = 1 ORDER BY date ASC LIMIT 10")
    suspend fun getFeaturedEvents(): List<Event>

    @Query("SELECT DISTINCT category FROM events")
    suspend fun getAllCategories(): List<String>

    @Update
    suspend fun updateEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: Int)
}