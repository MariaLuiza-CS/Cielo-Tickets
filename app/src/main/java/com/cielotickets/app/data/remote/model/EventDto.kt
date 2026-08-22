package com.cielotickets.app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class EventDto(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val availableTickets: Int = 0
)
