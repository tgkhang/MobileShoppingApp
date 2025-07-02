package com.example.shopapp.data.repository

import com.example.shopapp.data.dao.admin.IOrderDao
import com.example.shopapp.data.model.Order
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: IOrderDao) : IRepository<Order> {
    override suspend fun fetchAll(): Flow<List<Order>> {
        return orderDao.getAll()
    }

    override suspend fun fetchById(id: String): Order? {
        return orderDao.getById(id)
    }

    override suspend fun create(item: Order): Boolean {
        return orderDao.add(item)
    }

    override suspend fun modify(item: Order): Boolean {
        return orderDao.update(item)
    }

    override suspend fun remove(id: String): Boolean {
        return orderDao.delete(id)
    }

    suspend fun searchOrdersByUserId(userId: String): Flow<List<Order>> {
        return orderDao.searchOrdersByUserId(userId)
    }

    suspend fun searchOrdersByUsername(username: String): Flow<List<Order>> {
        return orderDao.searchOrdersByUsername(username)
    }

    fun getOrderDao(): IOrderDao = orderDao

    suspend fun getAllOrderByStatus(status: String): Flow<List<Order>> {
        return orderDao.getAllOrdersByStatus(status)
    }
}