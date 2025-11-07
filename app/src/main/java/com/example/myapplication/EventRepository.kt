package com.example.myapplication

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import at.favre.lib.crypto.bcrypt.BCrypt

class EventRepository(
    private val eventDao: EventDao,
    private val savedEventDao: SavedEventDao,
    private val userDao: UserDao
) {

    // Add this field to track current user
    private var currentUser: User? = null

    // Event operations
    suspend fun getAllEvents(): List<Event> = eventDao.getAllEvents()

    suspend fun getEventsByCategory(category: String): List<Event> =
        eventDao.getEventsByCategory(category)

    suspend fun searchEvents(query: String): List<Event> =
        eventDao.searchEvents(query)

    suspend fun getFeaturedEvents(): List<Event> =
        eventDao.getFeaturedEvents()

    suspend fun getAllCategories(): List<String> =
        eventDao.getAllCategories()

    suspend fun addEvent(event: Event): Long =
        eventDao.insertEvent(event)

    // Saved events operations
    suspend fun saveEventForUser(userId: Int, eventId: Int) {
        savedEventDao.saveEvent(SavedEvent(userId, eventId))
    }

    suspend fun unsaveEventForUser(userId: Int, eventId: Int) {
        savedEventDao.unsaveEvent(SavedEvent(userId, eventId))
    }

    suspend fun getSavedEvents(userId: Int): List<Event> =
        savedEventDao.getSavedEvents(userId)

    suspend fun isEventSaved(userId: Int, eventId: Int): Boolean =
        savedEventDao.isEventSaved(userId, eventId) > 0

    // User operations
    suspend fun registerUser(username: String, email: String, password: String): Boolean {
        // Check if user already exists
        if (userDao.getUserByEmail(email) != null || userDao.getUserByUsername(username) != null) {
            return false
        }

        // Hash password using bcrypt
        val passwordHash = BCrypt.withDefaults()
            .hashToString(12, password.toCharArray())

        val user = User(
            username = username,
            email = email,
            passwordHash = passwordHash
        )

        return try {
            userDao.insertUser(user) > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginUser(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email) ?: return null

        // Verify password using bcrypt
        val result = BCrypt.verifyer()
            .verify(password.toCharArray(), user.passwordHash)

        return if (result.verified) {
            currentUser = user // Set current user on successful login
            user
        } else null
    }

    // Add this method to get current user
    fun getCurrentUser(): User? = currentUser

    // Add this method to set current user to null on logout
    fun logout() {
        currentUser = null
    }

    suspend fun updateUserPreferences(userId: Int, preferences: List<String>) {
        val user = userDao.getUserById(userId) ?: return
        val updatedUser = user.copy(preferences = preferences.joinToString(","))
        userDao.updateUser(updatedUser)
    }

    suspend fun updateUserLanguage(userId: Int, language: String) {
        val user = userDao.getUserById(userId) ?: return
        val updatedUser = user.copy(language = language)
        userDao.updateUser(updatedUser)
    }

    suspend fun getUserById(userId: Int): User? = userDao.getUserById(userId)
}