package com.cielotickets.app.domain.usecase

import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.repository.EventRepository
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(private val repository: EventRepository) {
    suspend operator fun invoke(id: String): Result<Event> = repository.getEventById(id)
}
