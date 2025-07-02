package com.example.shopapp.data.repository

import kotlinx.coroutines.flow.Flow

interface IRepository<T> {
    suspend fun fetchAll(): Flow<List<T>>
    suspend fun fetchById(id: String): T?
    suspend fun create(item: T): Boolean
    suspend fun modify(item: T): Boolean
    suspend fun remove(id: String): Boolean
}
