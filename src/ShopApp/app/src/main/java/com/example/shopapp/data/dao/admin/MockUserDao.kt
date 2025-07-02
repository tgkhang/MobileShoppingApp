package com.example.shopapp.data.dao.admin

import com.example.shopapp.data.model.User
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockUserDao : IUserDao {
    private val users = mutableListOf<User>()

    init {
        // Create mock users
        users.addAll(listOf(
            User(
                userId = "user1",
                username = "john_doe",
                email = "john@example.com",
                address = "123 Main St, City",
                phone = "555-123-4567",
                role = "user",
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user2",
                username = "jane_smith",
                email = "jane@example.com",
                address = "456 Oak Ave, Town",
                phone = "555-987-6543",
                role = "admin",
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user3",
                username = "mike_wilson",
                email = "mike@example.com",
                address = "789 Pine St, Village",
                phone = "555-555-5555",
                role = "user",
                status = "inactive",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user4",
                username = "alice_johnson",
                email = "alice@example.com",
                address = "101 Maple Dr, Suburb",
                phone = "555-111-2222",
                role = "user",
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user5",
                username = "david_brown",
                email = "david@example.com",
                address = "202 Elm St, Metropolis",
                phone = "555-333-4444",
                role = "moderator",
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user6",
                username = "sarah_lee",
                email = "sarah@example.com",
                address = "303 Cedar Ln, Downtown",
                phone = "555-444-5555",
                role = "user",
                status = "suspended",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user7",
                username = "robert_garcia",
                email = "robert@example.com",
                address = "404 Birch Rd, Uptown",
                phone = "555-666-7777",
                role = "user",
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user8",
                username = "emma_taylor",
                email = "emma@example.com",
                address = "505 Walnut Ave, Countryside",
                phone = "555-777-8888",
                role = "vip",
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user9",
                username = "kevin_white",
                email = "kevin@example.com",
                address = "606 Pine Blvd, Heights",
                phone = "555-888-9999",
                role = "user",
                status = "inactive",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ),
            User(
                userId = "user10",
                username = "olivia_martinez",
                email = "olivia@example.com",
                address = "707 Oak Circle, Riverside",
                phone = "555-999-0000",
                role = "admin",
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        ))
    }

    override suspend fun getAll(): Flow<List<User>> {
        return flowOf(users)
    }

    override suspend fun getById(id: String): User? {
        return users.find { it.userId == id }
    }

    override suspend fun add(item: User): Boolean {
        return if (users.any { it.userId == item.userId }) {
            false
        } else {
            users.add(item)
            true
        }
    }

    override suspend fun update(item: User): Boolean {
        val index = users.indexOfFirst { it.userId == item.userId }
        return if (index != -1) {
            users[index] = item
            true
        } else {
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return users.removeIf { it.userId == id }
    }

    override suspend fun searchUsersByKeyword(keyword: String, limit: Int, offset: Int): Flow<List<User>> {
        val filteredUsers = users.filter {
            it.username.contains(keyword, ignoreCase = true) ||
            it.email.contains(keyword, ignoreCase = true)
        }.drop(offset).take(limit)
        return flowOf(filteredUsers)
    }

    override suspend fun getUsersPage(limit: Int, offset: Int): Flow<List<User>> {
        val paginatedUsers = users.drop(offset).take(limit)
        return flowOf(paginatedUsers)
    }

    override suspend fun getTotalUsersCount(): Int {
        return users.size
    }

    override suspend fun getTotalUsersCountByKeyword(keyword: String): Int {
        return users.count {
            it.username.contains(keyword, ignoreCase = true) ||
            it.email.contains(keyword, ignoreCase = true)
        }
    }

    override suspend fun searchUserByStatus(status: String, limit: Int, offset: Int): Flow<List<User>> {
        val filteredUsers = users.filter { it.status == status }
            .drop(offset)
            .take(limit)
        return flowOf(filteredUsers)
    }

    override suspend fun getTotalUsersCountByStatus(status: String): Int {
        return users.count { it.status == status }
    }
}