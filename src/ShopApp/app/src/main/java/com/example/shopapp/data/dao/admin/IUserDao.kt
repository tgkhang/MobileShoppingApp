package com.example.shopapp.data.dao.admin

import com.example.shopapp.data.dao.IDao
import com.example.shopapp.data.model.User
import kotlinx.coroutines.flow.Flow

interface IUserDao : IDao<User> {
    suspend fun searchUsersByKeyword(keyword: String, limit: Int, offset: Int): Flow<List<User>>
    suspend fun getUsersPage(limit: Int, offset: Int): Flow<List<User>>
    suspend fun searchUserByStatus(status: String, limit: Int, offset: Int): Flow<List<User>>
    suspend fun getTotalUsersCount(): Int
    suspend fun getTotalUsersCountByKeyword(keyword: String): Int
    suspend fun getTotalUsersCountByStatus(status: String): Int
}