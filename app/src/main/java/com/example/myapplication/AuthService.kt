package com.example.myapplication

import at.favre.lib.crypto.bcrypt.BCrypt

class AuthService(private val userDao: UserDao) {

    suspend fun registerUser(username: String, email: String, password: String): Boolean {
        // Check if user already exists
        if (userDao.getUserByEmail(email) != null || userDao.getUserByUsername(username) != null) {
            return false
        }

        // Hash password
        val passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())

        // Create user
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

        val result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash)
        return if (result.verified) user else null
    }

    suspend fun updateUserPreferences(userId: Int, preferences: List<String>) {
        val user = userDao.getUserById(userId) ?: return
        // Convert List<String> to String (comma-separated)
        val preferencesString = preferences.joinToString(",")
        val updatedUser = user.copy(preferences = preferencesString)
        userDao.updateUser(updatedUser)
    }

    suspend fun updateUserLanguage(userId: Int, language: String) {
        val user = userDao.getUserById(userId) ?: return
        val updatedUser = user.copy(language = language)
        userDao.updateUser(updatedUser)
    }

    // Helper method to convert preferences string back to list
    fun getPreferencesList(preferencesString: String): List<String> {
        return if (preferencesString.isBlank()) {
            emptyList()
        } else {
            preferencesString.split(",")
        }
    }
}