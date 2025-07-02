package com.example.shopapp.data.dao.admin

import android.util.Log
import com.example.shopapp.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class RealUserDao : IUserDao {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val TAG = "RealUserDao"

    // Helper function to convert Firestore document to User
    private fun documentToUser(doc: com.google.firebase.firestore.DocumentSnapshot): User? {
        return try {
            val userId = doc.getString("userId") ?: doc.id
            val username = doc.getString("username") ?: ""
            val email = doc.getString("email") ?: ""
            val address = doc.getString("address") ?: ""
            val phone = doc.getString("phone") ?: ""
            val role = doc.getString("role") ?: "user"
            val status = doc.getString("status") ?: "active"
            val createdAt = doc.getTimestamp("createdAt")
            val updatedAt = doc.getTimestamp("updatedAt")

            User(
                userId = userId,
                username = username,
                email = email,
                address = address,
                phone = phone,
                role = role,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing user document: ${e.message}")
            null
        }
    }

    override suspend fun getAll(): Flow<List<User>> = flow {
        try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { documentToUser(it) }
            Log.d(TAG, "Fetched all users: ${users.size}")
            emit(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all users: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getById(id: String): User? {
        return try {
            val doc = usersCollection.document(id).get().await()
            val user = documentToUser(doc)
            Log.d(TAG, "Fetched user by ID: ${user?.username}")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by ID: ${e.message}")
            null
        }
    }

    override suspend fun add(item: User): Boolean {
        return try {
            val newUser = item.copy(
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            usersCollection.document(item.userId).set(newUser).await()
            Log.d(TAG, "User added successfully: ${item.userId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user", e)
            false
        }
    }

    override suspend fun update(item: User): Boolean {
        return try {
            val updatedUser = item.copy(
                updatedAt = Timestamp.now()
            )
            usersCollection.document(item.userId).set(updatedUser).await()
            Log.d(TAG, "User updated successfully: ${item.userId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            usersCollection.document(id).delete().await()
            Log.d(TAG, "User deleted successfully: $id")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user", e)
            false
        }
    }

    override suspend fun searchUsersByKeyword(keyword: String, limit: Int, offset: Int): Flow<List<User>> = flow {
        try {
            val lowercaseKeyword = keyword.lowercase()
            val matchingUsers = mutableListOf<User>()
            val userIds = mutableSetOf<String>()

            if (lowercaseKeyword.isNotEmpty()) {
                val firstChar = lowercaseKeyword.first().toString()
                val firstCharCapitalized = firstChar.uppercase()

                // Search with lowercase first char in username
                val lowerUsernameResults = usersCollection
                    .whereGreaterThanOrEqualTo("username", firstChar)
                    .whereLessThanOrEqualTo("username", firstChar + "\uf8ff")
                    .get().await()

                // Search with uppercase first char in username
                val upperUsernameResults = usersCollection
                    .whereGreaterThanOrEqualTo("username", firstCharCapitalized)
                    .whereLessThanOrEqualTo("username", firstCharCapitalized + "\uf8ff")
                    .get().await()

                // Search in email
                val emailResults = usersCollection
                    .whereGreaterThanOrEqualTo("email", keyword)
                    .whereLessThanOrEqualTo("email", keyword + "\uf8ff")
                    .get().await()

                // Process results and filter client-side
                for (snapshot in listOf(lowerUsernameResults, upperUsernameResults, emailResults)) {
                    snapshot.documents.forEach { doc ->
                        if (!userIds.contains(doc.id)) {
                            documentToUser(doc)?.let { user ->
                                if (user.username.lowercase().contains(lowercaseKeyword) ||
                                    user.email.lowercase().contains(lowercaseKeyword)
                                ) {
                                    userIds.add(doc.id)
                                    matchingUsers.add(user)
                                }
                            }
                        }
                    }
                }
            }

            // Apply pagination (offset and limit) to the combined results
            val paginatedResults = matchingUsers
                .drop(offset)
                .take(limit)

            Log.d(TAG, "Search results for keyword '$keyword': ${paginatedResults.size}")
            emit(paginatedResults)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users by keyword: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getUsersPage(limit: Int, offset: Int): Flow<List<User>> = flow {
        try {
            val query = if (offset == 0) {
                usersCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = usersCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    usersCollection
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<User>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val users = snapshot.documents.mapNotNull { documentToUser(it) }
            Log.d(TAG, "Fetched page with limit=$limit, offset=$offset: ${users.size} users")
            emit(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users page: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun searchUserByStatus(status: String, limit: Int, offset: Int): Flow<List<User>> = flow {
        try {
            val baseQuery = usersCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            val query = if (offset == 0) {
                baseQuery.limit(limit.toLong())
            } else {
                val lastVisibleDocSnapshot = baseQuery
                    .limit(offset.toLong())
                    .get()
                    .await()
                    .documents
                    .lastOrNull()

                if (lastVisibleDocSnapshot != null) {
                    baseQuery
                        .startAfter(lastVisibleDocSnapshot)
                        .limit(limit.toLong())
                } else {
                    emit(emptyList<User>())
                    return@flow
                }
            }

            val snapshot = query.get().await()
            val users = snapshot.documents.mapNotNull { documentToUser(it) }
            Log.d(TAG, "Fetched users with status=$status, limit=$limit, offset=$offset: ${users.size}")
            emit(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users by status: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getTotalUsersCount(): Int {
        return try {
            val snapshot = usersCollection.get().await()
            val count = snapshot.size()
            Log.d(TAG, "Total users count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total users count: ${e.message}")
            0
        }
    }

    override suspend fun getTotalUsersCountByKeyword(keyword: String): Int {
        return try {
            val lowercaseKeyword = keyword.lowercase()
            val userIds = mutableSetOf<String>()

            if (lowercaseKeyword.isNotEmpty()) {
                val firstChar = lowercaseKeyword.first().toString()
                val firstCharCapitalized = firstChar.uppercase()

                // Search with lowercase first char in username
                val lowerUsernameResults = usersCollection
                    .whereGreaterThanOrEqualTo("username", firstChar)
                    .whereLessThanOrEqualTo("username", firstChar + "\uf8ff")
                    .get().await()

                // Search with uppercase first char in username
                val upperUsernameResults = usersCollection
                    .whereGreaterThanOrEqualTo("username", firstCharCapitalized)
                    .whereLessThanOrEqualTo("username", firstCharCapitalized + "\uf8ff")
                    .get().await()

                // Search in email
                val emailResults = usersCollection
                    .whereGreaterThanOrEqualTo("email", keyword)
                    .whereLessThanOrEqualTo("email", keyword + "\uf8ff")
                    .get().await()

                for (snapshot in listOf(lowerUsernameResults, upperUsernameResults, emailResults)) {
                    snapshot.documents.forEach { doc ->
                        if (!userIds.contains(doc.id)) {
                            documentToUser(doc)?.let { user ->
                                if (user.username.lowercase().contains(lowercaseKeyword) ||
                                    user.email.lowercase().contains(lowercaseKeyword)
                                ) {
                                    userIds.add(doc.id)
                                }
                            }
                        }
                    }
                }
            }

            val count = userIds.size
            Log.d(TAG, "Total users count for keyword '$keyword': $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users count by keyword: ${e.message}")
            0
        }
    }

    override suspend fun getTotalUsersCountByStatus(status: String): Int {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("status", status)
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total users count with status '$status': $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users count by status: ${e.message}")
            0
        }
    }
}