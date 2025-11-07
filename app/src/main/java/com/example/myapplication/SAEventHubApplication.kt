package com.example.myapplication

import android.app.Application

class SAEventHubApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val eventRepository: EventRepository by lazy {
        val eventDao = database.eventDao()
        val savedEventDao = database.savedEventDao()
        val userDao = database.userDao()
        EventRepository(eventDao, savedEventDao, userDao)
    }
}