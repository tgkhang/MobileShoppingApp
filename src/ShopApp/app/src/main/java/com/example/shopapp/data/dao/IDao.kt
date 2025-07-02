package com.example.shopapp.data.dao

import kotlinx.coroutines.flow.Flow

interface IDao<T> {
    suspend fun getAll(): Flow<List<T>>
    suspend fun getById(id: String): T?
    suspend fun add(item: T): Boolean
    suspend fun update(item: T): Boolean
    suspend fun delete(id: String): Boolean
}