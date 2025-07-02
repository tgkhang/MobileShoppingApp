package com.example.shopapp.data.dao

import android.util.Log
import com.example.shopapp.data.model.NotificationItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseNotificationDao : INotificationDao {
    private val db = FirebaseFirestore.getInstance()
    private val notificationsCollection = db.collection("notifications")
    private val TAG = "FirebaseNotificationDao"

    // Helper function to convert Firestore document to NotificationItem
    private fun documentToNotification(doc: com.google.firebase.firestore.DocumentSnapshot): NotificationItem? {
        return try {
            val data = doc.data
            if (data != null) {
                NotificationItem(
                    id = doc.id,
                    userId = data["userId"] as? String ?: "",
                    title = data["title"] as? String ?: "",
                    message = data["message"] as? String ?: "",
                    orderId = data["orderId"] as? String ?: "",
                    timestamp = data["timestamp"] as? Long ?: 0L,
                    isRead = data["isRead"] as? Boolean ?: false
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing notification document: ${e.message}")
            null
        }
    }

    override suspend fun getNotificationsByUserId(userId: String): Flow<List<NotificationItem>> = flow {
        try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val notifications = snapshot.documents.mapNotNull { documentToNotification(it) }
            Log.d(TAG, "Fetched ${notifications.size} notifications for user $userId")
            emit(notifications)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getUnreadNotificationCountByUserId(userId: String): Int {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val count = snapshot.size()
            Log.d(TAG, "Unread notifications count for user $userId: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread notifications count: ${e.message}")
            0
        }
    }

    override suspend fun updateNotification(notification: NotificationItem): Boolean {
        return try {
            val updateData = mapOf(
                "userId" to notification.userId,
                "title" to notification.title,
                "message" to notification.message,
                "orderId" to notification.orderId,
                "timestamp" to notification.timestamp,
                "isRead" to notification.isRead
            )

            notificationsCollection.document(notification.id)
                .update(updateData)
                .await()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification: ${e.message}")
            false
        }
    }

    override suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Log.d(TAG, "Notification deleted successfully: $notificationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification: ${e.message}")
            false
        }
    }
}