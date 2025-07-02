package com.example.shopapp.data.repository

import com.example.shopapp.data.dao.IOrderDaoFirebase
import com.example.shopapp.data.model.OrderFirebase
import kotlinx.coroutines.flow.Flow

class OrderRepositoryFirebase(private val dao: IOrderDaoFirebase) : IRepository<OrderFirebase> {
    override suspend fun fetchAll(): Flow<List<OrderFirebase>> {
        return dao.getAll()
    }

    override suspend fun fetchById(id: String): OrderFirebase? {
        return dao.getById(id)
    }

    override suspend fun create(item: OrderFirebase): Boolean {
        return dao.add(item)
    }

    override suspend fun modify(item: OrderFirebase): Boolean {
        return dao.update(item)
    }

    override suspend fun remove(id: String): Boolean {
        return dao.delete(id)
    }

    suspend fun searchOrdersByUserId(userId: String): Flow<List<OrderFirebase>> {
        return dao.searchOrdersByUserId(userId)
    }

    suspend fun searchOrdersByUsername(username: String): Flow<List<OrderFirebase>> {
        return dao.searchOrdersByUsername(username)
    }
}