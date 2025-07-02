package com.example.shopapp.data.repository

import com.example.shopapp.data.dao.INotificationDao
import com.example.shopapp.data.model.NotificationItem
import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val notificationDao: INotificationDao
) {
    suspend fun getUserNotifications(userId: String): Flow<List<NotificationItem>> {
        return notificationDao.getNotificationsByUserId(userId)
    }

    suspend fun getUnreadNotificationsCount(userId: String): Int {
        return notificationDao.getUnreadNotificationCountByUserId(userId)
    }

    suspend fun markNotificationAsRead(notification: NotificationItem): Boolean {
        val updatedNotification = notification.copy(isRead = true)
        return notificationDao.updateNotification(updatedNotification)
    }

    suspend fun deleteNotification(notificationId: String): Boolean {
        return notificationDao.deleteNotification(notificationId)
    }
}