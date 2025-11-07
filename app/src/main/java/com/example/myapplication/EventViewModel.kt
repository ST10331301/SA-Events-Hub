package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _savedEvents = MutableStateFlow<List<Event>>(emptyList())
    val savedEvents = _savedEvents.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Event>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    init {
        loadAllEvents()
        loadCategories()
    }

    fun loadAllEvents() {
        viewModelScope.launch {
            try {
                _events.value = repository.getAllEvents()
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }

    fun loadEventsByCategory(category: String) {
        viewModelScope.launch {
            try {
                _events.value = repository.getEventsByCategory(category)
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }

    fun searchEvents(query: String) {
        viewModelScope.launch {
            try {
                _searchResults.value = repository.searchEvents(query)
                // If we have search results, show them instead of regular events
                if (_searchResults.value.isNotEmpty()) {
                    _events.value = _searchResults.value
                }
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }

    fun loadSavedEvents(userId: Int) {
        viewModelScope.launch {
            try {
                _savedEvents.value = repository.getSavedEvents(userId)
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }

    fun saveEvent(userId: Int, eventId: Int) {
        viewModelScope.launch {
            try {
                repository.saveEventForUser(userId, eventId)
                loadSavedEvents(userId)
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }

    fun unsaveEvent(userId: Int, eventId: Int) {
        viewModelScope.launch {
            try {
                repository.unsaveEventForUser(userId, eventId)
                loadSavedEvents(userId)
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            try {
                repository.addEvent(event)
                loadAllEvents() // This will trigger the events flow update
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = repository.getAllCategories()
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }

    fun registerUser(username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.registerUser(username, email, password)
                onResult(success)
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    fun loginUser(email: String, password: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.loginUser(email, password)
                _currentUser.value = user
                user?.let { loadSavedEvents(it.id) }
                onResult(user)
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _savedEvents.value = emptyList()
        repository.logout() // Call repository logout to clear current user
    }

    // Add this missing function
    fun isEventSaved(eventId: Int): Boolean {
        val user = _currentUser.value ?: return false
        return _savedEvents.value.any { it.id == eventId }
    }
}