package com.example.shopapp.data.dao.admin

import android.util.Log
import com.example.shopapp.data.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class RealEventDao : IEventDao {
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")
    private val TAG = "RealEventDao"

    override suspend fun getAll(): Flow<List<Event>> = flow {
        try {
            val snapshot = eventsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)
            }
            Log.d(TAG, "Total events fetched: ${events.size}")
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getById(id: String): Event? {
        return try {
            val doc = eventsCollection.document(id).get().await()
            doc.toObject(Event::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching event by ID: ${e.message}")
            null
        }
    }

    override suspend fun add(item: Event): Boolean {
        return try {
            eventsCollection.document(item.eventId).set(item).await()
            Log.d(TAG, "Event added successfully: ${item.eventId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding event", e)
            false
        }
    }

    override suspend fun update(item: Event): Boolean {
        return try {
            eventsCollection.document(item.eventId).set(item).await()
            Log.d(TAG, "Event updated successfully: ${item.eventId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event", e)
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            eventsCollection.document(id).delete().await()
            Log.d(TAG, "Event deleted successfully: $id")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event", e)
            false
        }
    }

    override suspend fun searchEventsByTitle(title: String): Flow<List<Event>> = flow {
        try {
            if (title.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            val lowercaseTitle = title.lowercase()
            val firstChar = lowercaseTitle.first().toString()
            val firstCharCapitalized = firstChar.uppercase()

            val results = mutableListOf<Event>()
            val eventIds = mutableSetOf<String>()

            // Search with lowercase first char
            val lowerResults = eventsCollection
                .whereGreaterThanOrEqualTo("title", firstChar)
                .whereLessThanOrEqualTo("title", firstChar + "\uf8ff")
                .get().await()

            // Search with uppercase first char
            val upperResults = eventsCollection
                .whereGreaterThanOrEqualTo("title", firstCharCapitalized)
                .whereLessThanOrEqualTo("title", firstCharCapitalized + "\uf8ff")
                .get().await()

            // Process results and filter client-side
            for (snapshot in listOf(lowerResults, upperResults)) {
                snapshot.documents.forEach { doc ->
                    if (!eventIds.contains(doc.id)) {
                        doc.toObject(Event::class.java)?.let { event ->
                            if (event.title.lowercase().contains(lowercaseTitle)) {
                                eventIds.add(doc.id)
                                results.add(event)
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "Search results for title '$title': ${results.size}")
            emit(results)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching events by title: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun searchEventsByDescription(description: String): Flow<List<Event>> = flow {
        try {
            if (description.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            // We need to fetch all events and filter client-side because
            // Firebase doesn't support full text search on fields
            val snapshot = eventsCollection.get().await()

            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)
            }.filter {
                it.description.contains(description, ignoreCase = true)
            }

            Log.d(TAG, "Search results for description '$description': ${events.size}")
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching events by description: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun searchEventsByType(eventType: String): Flow<List<Event>> = flow {
        try {
            val snapshot = eventsCollection
                .whereEqualTo("eventType", eventType)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)
            }

            Log.d(TAG, "Search results for event type '$eventType': ${events.size}")
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching events by type: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun searchEventsByStatus(status: String): Flow<List<Event>> = flow {
        try {
            val snapshot = eventsCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)
            }

            Log.d(TAG, "Search results for status '$status': ${events.size}")
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching events by status: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getEventsPage(limit: Int, offset: Int): Flow<List<Event>> = flow {
        try {
            val query = if (offset == 0) {
                eventsCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = eventsCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    eventsCollection
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Event>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)
            }

            Log.d(TAG, "Fetched page with limit=$limit, offset=$offset: ${events.size} events")
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events page: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getEventsPageByType(eventType: String, limit: Int, offset: Int): Flow<List<Event>> = flow {
        try {
            val baseQuery = eventsCollection
                .whereEqualTo("eventType", eventType)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            val query = if (offset == 0) {
                baseQuery.limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = baseQuery
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    baseQuery
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Event>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)
            }

            Log.d(TAG, "Fetched type=$eventType page with limit=$limit, offset=$offset: ${events.size} events")
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events page by type: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getEventsPageByStatus(status: String, limit: Int, offset: Int): Flow<List<Event>> = flow {
        try {
            val baseQuery = eventsCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            val query = if (offset == 0) {
                baseQuery.limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = baseQuery
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    baseQuery
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<Event>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val events = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)
            }

            Log.d(TAG, "Fetched status=$status page with limit=$limit, offset=$offset: ${events.size} events")
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events page by status: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getTotalEventsCount(): Int {
        return try {
            val snapshot = eventsCollection.get().await()
            val count = snapshot.size()
            Log.d(TAG, "Total events count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total events count: ${e.message}")
            0
        }
    }

    override suspend fun getTotalEventsCountByType(eventType: String): Int {
        return try {
            val snapshot = eventsCollection
                .whereEqualTo("eventType", eventType)
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total events count for type '$eventType': $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting events count by type: ${e.message}")
            0
        }
    }

    override suspend fun getTotalEventsCountByStatus(status: String): Int {
        return try {
            val snapshot = eventsCollection
                .whereEqualTo("status", status)
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total events count for status '$status': $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting events count by status: ${e.message}")
            0
        }
    }
}