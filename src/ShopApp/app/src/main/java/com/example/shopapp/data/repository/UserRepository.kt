package com.example.shopapp.data.repository

import com.example.shopapp.data.dao.admin.IUserDao
import com.example.shopapp.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: IUserDao) : IRepository<User> {
    override suspend fun fetchAll(): Flow<List<User>> {
        return userDao.getAll()
    }

    override suspend fun fetchById(id: String): User? {
        return userDao.getById(id)
    }

    override suspend fun create(item: User): Boolean {
        return userDao.add(item)
    }

    override suspend fun modify(item: User): Boolean {
        return userDao.update(item)
    }

    override suspend fun remove(id: String): Boolean {
        return userDao.delete(id)
    }

    suspend fun getUserDao(): IUserDao = userDao
}