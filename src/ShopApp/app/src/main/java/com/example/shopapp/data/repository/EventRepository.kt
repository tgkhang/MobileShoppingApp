package com.example.shopapp.data.repository

import com.example.shopapp.data.dao.admin.IEventDao
import com.example.shopapp.data.model.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: IEventDao) : IRepository<Event> {
    override suspend fun fetchAll(): Flow<List<Event>> {
        return eventDao.getAll()
    }

    override suspend fun fetchById(id: String): Event? {
        return eventDao.getById(id)
    }

    override suspend fun create(item: Event): Boolean {
        return eventDao.add(item)
    }

    override suspend fun modify(item: Event): Boolean {
        return eventDao.update(item)
    }

    override suspend fun remove(id: String): Boolean {
        return eventDao.delete(id)
    }

    suspend fun searchEventsByTitle(title: String): Flow<List<Event>> {
        return eventDao.searchEventsByTitle(title)
    }

    suspend fun searchEventsByDescription(description: String): Flow<List<Event>> {
        return eventDao.searchEventsByDescription(description)
    }

    suspend fun searchEventsByType(eventType: String): Flow<List<Event>> {
        return eventDao.searchEventsByType(eventType)
    }

    suspend fun searchEventsByStatus(status: String): Flow<List<Event>> {
        return eventDao.searchEventsByStatus(status)
    }

    fun getEventDao(): IEventDao = eventDao
}