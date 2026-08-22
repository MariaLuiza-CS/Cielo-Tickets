package com.cielotickets.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cielotickets.app.data.local.dao.EventDao
import com.cielotickets.app.data.local.dao.PendingPurchaseDao
import com.cielotickets.app.data.local.dao.TicketDao
import com.cielotickets.app.data.local.entity.EventEntity
import com.cielotickets.app.data.local.entity.PendingPurchaseEntity
import com.cielotickets.app.data.local.entity.TicketEntity

@Database(
    entities = [
        EventEntity::class,
        PendingPurchaseEntity::class,
        TicketEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun pendingPurchaseDao(): PendingPurchaseDao
    abstract fun ticketDao(): TicketDao
}
