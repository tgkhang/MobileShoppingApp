package com.example.shopapp.data.model

import com.google.firebase.Timestamp

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val orderId: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false,
) {
    constructor() : this(
        id = "",
        userId = "",
        title = "",
        message = "",
        orderId = "",
        timestamp = Timestamp.now().seconds,
        isRead = false
    )

    fun getFormattedDate(): java.util.Date {
        return java.util.Date(timestamp)
    }
}