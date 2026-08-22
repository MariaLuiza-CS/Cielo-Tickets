package com.cielotickets.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cielotickets.app.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: TicketEntity)

    @Query("SELECT * FROM tickets")
    fun getAll(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE ticketId = :id")
    fun getById(id: String): Flow<TicketEntity?>

    @Query("SELECT * FROM tickets WHERE purchaseReference = :reference LIMIT 1")
    suspend fun getByPurchaseReference(reference: String): TicketEntity?
}
