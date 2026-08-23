package com.cielotickets.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cielotickets.app.data.local.entity.PendingPurchaseEntity

@Dao
interface PendingPurchaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(purchase: PendingPurchaseEntity)

    @Query("SELECT * FROM pending_purchases WHERE idempotencyKey = :key")
    suspend fun getByKey(key: String): PendingPurchaseEntity?

    @Query("UPDATE pending_purchases SET status = :status WHERE idempotencyKey = :key")
    suspend fun updateStatus(key: String, status: String)

    @Query("SELECT * FROM pending_purchases WHERE eventId = :eventId AND quantity = :quantity AND status = 'PENDING' ORDER BY createdAt DESC LIMIT 1")
    suspend fun findPendingByEventAndQuantity(eventId: String, quantity: Int): PendingPurchaseEntity?
}
