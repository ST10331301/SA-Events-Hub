package com.example.myapplication

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

import kotlinx.coroutines.launch
import java.util.*

class DatabaseInitializer(private val context: Context, private val scope: CoroutineScope) {

    fun initializeSampleData() {
        scope.launch {
            val database = AppDatabase.getInstance(context)
            val eventDao = database.eventDao()

            val sampleEvents = listOf(
                Event(
                    title = "Johannesburg Jazz Festival",
                    description = "Annual jazz festival featuring local and international artists. Experience the best of South African jazz music in the heart of Johannesburg.",
                    date = Date().time + 86400000L * 7,
                    location = "Maboneng Precinct, Johannesburg",
                    category = "Music",
                    price = 150.0f,
                    imageUrl = "",
                    isFeatured = true
                ),
                Event(
                    title = "Cape Town Marathon",
                    description = "Run through the beautiful streets of Cape Town in this annual marathon. Suitable for all fitness levels.",
                    date = Date().time + 86400000L * 14,
                    location = "Green Point, Cape Town",
                    category = "Sports",
                    price = 200.0f,
                    imageUrl = "",
                    isFeatured = true
                ),
                Event(
                    title = "Durban Food Festival",
                    description = "Celebration of diverse South African cuisine with local chefs and food vendors. Taste the flavors of Durban!",
                    date = Date().time + 86400000L * 21,
                    location = "Durban Beachfront",
                    category = "Food",
                    price = 50.0f,
                    imageUrl = ""
                ),
                Event(
                    title = "Pretoria Art Exhibition",
                    description = "Contemporary art exhibition featuring works from emerging South African artists.",
                    date = Date().time + 86400000L * 10,
                    location = "Pretoria Art Museum",
                    category = "Arts",
                    price = 80.0f,
                    imageUrl = ""
                )
            )

            sampleEvents.forEach { event ->
                eventDao.insertEvent(event)
            }
        }
    }
}