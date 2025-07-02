package com.example.shopapp.data.dao.admin

import com.example.shopapp.data.dao.IDao
import com.example.shopapp.data.model.Event
import kotlinx.coroutines.flow.Flow

interface IEventDao : IDao<Event> {
    suspend fun searchEventsByTitle(title: String): Flow<List<Event>>
    suspend fun searchEventsByDescription(description: String): Flow<List<Event>>
    suspend fun searchEventsByType(eventType: String): Flow<List<Event>>
    suspend fun searchEventsByStatus(status: String): Flow<List<Event>>

    // Pagination
    suspend fun getEventsPage(limit: Int, offset: Int): Flow<List<Event>>
    suspend fun getEventsPageByType(eventType: String, limit: Int, offset: Int): Flow<List<Event>>
    suspend fun getEventsPageByStatus(status: String, limit: Int, offset: Int): Flow<List<Event>>
    suspend fun getTotalEventsCount(): Int
    suspend fun getTotalEventsCountByType(eventType: String): Int
    suspend fun getTotalEventsCountByStatus(status: String): Int
}