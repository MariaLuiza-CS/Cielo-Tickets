package com.cielotickets.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketId: String,
    val eventId: String,
    val eventName: String,
    val purchaseReference: String,
    val cieloOrderId: String?,
    val qrPayload: String,
    val createdAt: Long
)
