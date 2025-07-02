package com.example.shopapp.data.dao

import com.example.shopapp.data.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface INotificationDao {
    suspend fun getNotificationsByUserId(userId: String): Flow<List<NotificationItem>>
    suspend fun getUnreadNotificationCountByUserId(userId: String): Int
    suspend fun updateNotification(notification: NotificationItem): Boolean
    suspend fun deleteNotification(notificationId: String): Boolean
}