package com.example.shopapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.NotificationItem
import com.example.shopapp.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                notificationRepository.getUserNotifications(userId).collect { notificationsList ->
                    _notifications.value = notificationsList
                }
                refreshUnreadCount(userId)
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to load notifications: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshUnreadCount(userId: String) {
        viewModelScope.launch {
            try {
                _unreadCount.value = notificationRepository.getUnreadNotificationsCount(userId)
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to refresh unread count: ${e.message}")
            }
        }
    }

    fun markAsRead(notification: NotificationItem) {
        if (notification.isRead) return

        viewModelScope.launch {
            try {
                val success = notificationRepository.markNotificationAsRead(notification)
                if (success) {
                    _notifications.value = _notifications.value.map {
                        if (it.id == notification.id) it.copy(isRead = true) else it
                    }
                    _unreadCount.value = _unreadCount.value - 1
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to mark notification as read: ${e.message}")
            }
        }
    }

    fun deleteNotification(notification: NotificationItem) {
        viewModelScope.launch {
            try {
                val success = notificationRepository.deleteNotification(notification.id)
                if (success) {
                    _notifications.value = _notifications.value.filter { it.id != notification.id }
                    if (!notification.isRead) {
                        _unreadCount.value = _unreadCount.value - 1
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failed to delete notification: ${e.message}")
            }
        }
    }

}