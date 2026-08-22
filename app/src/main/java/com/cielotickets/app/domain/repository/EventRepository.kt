package com.cielotickets.app.domain.repository

import com.cielotickets.app.domain.model.Event

interface EventRepository {
    suspend fun getEvents(): Result<List<Event>>
    suspend fun getEventById(id: String): Result<Event>
}
