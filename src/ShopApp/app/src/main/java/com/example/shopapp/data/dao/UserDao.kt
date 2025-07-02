package com.example.shopapp.data.dao

import com.example.shopapp.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserDao {
    suspend fun getAllUser(): Flow<List<User>>
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun addUser(user: User): Boolean
    suspend fun updateUser(user: User): Boolean
    suspend fun deleteUser(userId: String): Boolean

    //pagination optimize
    suspend fun getUserPage(limit: Int, offset: Int): Flow<List<User>>
    suspend fun searchUserPageByKeyword(keyword: String, limit: Int, offset: Int): Flow<List<User>>
    suspend fun getTotalUsersCount(): Int
    suspend fun getTotalUsersCountByKeyword(keyword: String): Int
}
