package com.cielotickets.app.data.remote.datasource

import com.cielotickets.app.data.remote.model.EventDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun fetchEvents(): List<EventDto> {
        return try {
            val snapshot = firestore.collection("events").get().await()
            snapshot.toObjects(EventDto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
