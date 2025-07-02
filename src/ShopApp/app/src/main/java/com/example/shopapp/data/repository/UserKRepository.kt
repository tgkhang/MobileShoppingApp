package com.example.shopapp.data.repository

import com.example.shopapp.data.dao.UserDao
import com.example.shopapp.data.model.User
import kotlinx.coroutines.flow.Flow

class UserKRepository(private val userDao: UserDao) {
    private val TAG = "UserRepository"

    suspend fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUser()
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun addUser(user: User): Boolean {
        return userDao.addUser(user)
    }

    suspend fun updateUser(user: User): Boolean {
        return userDao.updateUser(user)
    }

    suspend fun deleteUser(userId: String): Boolean {
        return userDao.deleteUser(userId)
    }

    suspend fun getUserPage(limit: Int, offset: Int): Flow<List<User>> {
        return userDao.getUserPage(limit, offset)
    }

    suspend fun searchUserPageByKeyword(keyword: String, limit: Int, offset: Int): Flow<List<User>> {
        return userDao.searchUserPageByKeyword(keyword, limit, offset)
    }

    suspend fun getTotalUsersCount(): Int {
        return userDao.getTotalUsersCount()
    }

    suspend fun getTotalUsersCountByKeyword(keyword: String): Int {
        return userDao.getTotalUsersCountByKeyword(keyword)
    }
}