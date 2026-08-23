package com.cielotickets.app.data.repository

import com.cielotickets.app.data.local.dao.EventDao
import com.cielotickets.app.data.mapper.toDomain
import com.cielotickets.app.data.mapper.toDomainList
import com.cielotickets.app.data.mapper.toEntityList
import com.cielotickets.app.data.remote.datasource.EventRemoteDataSource
import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.repository.EventRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(private val remoteDataSource: EventRemoteDataSource, private val eventDao: EventDao) : EventRepository {

    override suspend fun getEvents(): Result<List<Event>> = try {
        // Sincroniza dados remotos com local (Single Source of Truth)
        val remoteEvents = remoteDataSource.fetchEvents()
        if (remoteEvents.isNotEmpty()) {
            eventDao.clearAll()
            eventDao.insertEvents(remoteEvents.toEntityList())
        }

        // Retorna o que está no cache local
        val localEvents = eventDao.getAllEvents().first()
        Result.success(localEvents.toDomainList())
    } catch (e: Exception) {
        // Se falhar a rede, tenta retornar o cache local
        val localEvents = eventDao.getAllEvents().first()
        if (localEvents.isNotEmpty()) {
            Result.success(localEvents.toDomainList())
        } else {
            Result.failure(e)
        }
    }

    override suspend fun getEventById(id: String): Result<Event> = try {
        val localEvent = eventDao.getEventById(id)
        if (localEvent != null) {
            Result.success(localEvent.toDomain())
        } else {
            Result.failure(Exception("Evento não encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
