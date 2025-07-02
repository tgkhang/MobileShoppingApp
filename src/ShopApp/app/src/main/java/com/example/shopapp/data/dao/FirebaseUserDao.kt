package com.example.shopapp.data.dao

import android.util.Log
import com.example.shopapp.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseUserDao : UserDao {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val TAG = "FirebaseUserDao"

    // Helper function to convert Firestore document to User
    private fun documentToUser(doc: com.google.firebase.firestore.DocumentSnapshot): User? {
        return doc.toObject(User::class.java)
    }

    override suspend fun getAllUser(): Flow<List<User>> = flow {
        try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { documentToUser(it) }
            Log.d(TAG, "Total users fetched: ${users.size}")
            emit(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all users: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            documentToUser(doc)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by ID: ${e.message}")
            null
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return try {
            val snapshot = usersCollection.whereEqualTo("email", email).limit(1).get().await()
            if (snapshot.documents.isNotEmpty()) {
                documentToUser(snapshot.documents[0])
            } else {
                Log.d(TAG, "No user found with email: $email")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by email: ${e.message}")
            null
        }
    }

    override suspend fun getUserByUsername(username: String): User? {
        return try {
            val snapshot = usersCollection.whereEqualTo("username", username).limit(1).get().await()
            if (snapshot.documents.isNotEmpty()) {
                documentToUser(snapshot.documents[0])
            } else {
                Log.d(TAG, "No user found with username: $username")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by username: ${e.message}")
            null
        }
    }

    override suspend fun addUser(user: User): Boolean {
        return try {
            usersCollection.document(user.userId).set(user).await()
            Log.d(TAG, "User added successfully: ${user.userId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user", e)
            false
        }
    }

    override suspend fun updateUser(user: User): Boolean {
        return try {
            if (user.userId.isBlank()) {
                Log.e(TAG, "Cannot update user: userId is blank or null")

                // Try to find the user by email instead
                val emailQuery = usersCollection.whereEqualTo("email", user.email).limit(1).get().await()
                if (emailQuery.documents.isEmpty()) {
                    Log.e(TAG, "Cannot update user: No user found with email ${user.email}")
                    return false
                }

                val docId = emailQuery.documents[0].id
                usersCollection.document(docId).set(user.copy(userId = docId)).await()
                Log.d(TAG, "User updated successfully by email lookup: $docId")
                true
            } else {
                // Use the existing userId
                usersCollection.document(user.userId).set(user).await()
                Log.d(TAG, "User updated successfully: ${user.userId}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            false
        }
    }

    override suspend fun deleteUser(userId: String): Boolean {
        return try {
            usersCollection.document(userId).delete().await()
            Log.d(TAG, "User deleted successfully: ${userId}")
            true
        }
        catch (e: Exception){
            Log.e(TAG, "Error deleting user", e)
            false
        }
    }

    override suspend fun getUserPage(
        limit: Int,
        offset: Int
    ): Flow<List<User>> {
        TODO("Not yet implemented")
    }

    override suspend fun searchUserPageByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flow<List<User>> {
        TODO("Not yet implemented")
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
            val snapshot = usersCollection
                .whereGreaterThanOrEqualTo("username", keyword)
                .whereLessThanOrEqualTo("username", keyword + "\uf8ff")
                .get()
                .await()
            val count = snapshot.size()
            Log.d(TAG, "Total users count for keyword '$keyword': $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting keyword users count: ${e.message}")
            0
        }
    }
}