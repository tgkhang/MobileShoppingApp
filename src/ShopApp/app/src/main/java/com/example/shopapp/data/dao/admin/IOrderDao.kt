package com.example.shopapp.data.dao.admin

import com.example.shopapp.data.dao.IDao
import com.example.shopapp.data.model.Order
import kotlinx.coroutines.flow.Flow

interface IOrderDao : IDao<Order> {
    suspend fun searchOrdersByUserId(userId: String): Flow<List<Order>>
    suspend fun searchOrdersByUsername(username: String): Flow<List<Order>>

    suspend fun getOrdersPage(limit: Int, offset: Int): Flow<List<Order>>
    suspend fun getOrdersPageByUserId(userId: String, limit: Int, offset: Int): Flow<List<Order>>
    suspend fun getOrdersPageByStatus(status: String, limit: Int, offset: Int): Flow<List<Order>>

    suspend fun getTotalOrdersCount(): Int
    suspend fun getTotalOrdersCountByUserId(userId: String): Int
    suspend fun getTotalOrdersCountByStatus(status: String): Int

    suspend fun getAllOrdersByStatus(status: String): Flow<List<Order>>
}