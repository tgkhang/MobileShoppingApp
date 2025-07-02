package com.example.shopapp.data.dao

import com.example.shopapp.data.model.OrderFirebase
import kotlinx.coroutines.flow.Flow

interface IOrderDaoFirebase {
    suspend fun getAll(): Flow<List<OrderFirebase>>
    suspend fun getById(id: String): OrderFirebase?
    suspend fun add(item: OrderFirebase): Boolean
    suspend fun update(item: OrderFirebase): Boolean
    suspend fun delete(id: String): Boolean
    suspend fun searchOrdersByUserId(userId: String): Flow<List<OrderFirebase>>
    suspend fun searchOrdersByUsername(username: String): Flow<List<OrderFirebase>>
}
