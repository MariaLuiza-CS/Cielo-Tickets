package com.cielotickets.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_purchases")
data class PendingPurchaseEntity(
    @PrimaryKey val idempotencyKey: String,
    val eventId: String,
    val quantity: Int,
    val totalPriceCents: Int,
    val status: String,
    val createdAt: Long = System.currentTimeMillis()
)
