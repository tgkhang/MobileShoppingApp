package com.example.shopapp.data.model

import com.google.firebase.Timestamp

data class OrderFirebase(
    val orderId: String = "",
    val userId: String = "",
    val username: String = "",
    val phone: String = "",
    val address: String = "",
    val orderDetail: List<CartItemFirebase> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = "pending",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)