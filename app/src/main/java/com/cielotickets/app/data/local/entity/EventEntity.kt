package com.cielotickets.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val date: String,
    val location: String,
    val price: Double,
    val imageUrl: String,
    val availableTickets: Int,
)
