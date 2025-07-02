package com.example.shopapp.data.model

import com.google.firebase.Timestamp

data class Order(
    val orderId: String,
    val userId: String,
    val username: String,
    val phone: String,
    val address: String,
    val orderDetail: List<CartItem>,
    val totalPrice: Double,
    val status: String,
    val createdAt: Timestamp?,
    val updatedAt: Timestamp?
) {
    constructor() : this("", "", "", "", "", emptyList(), 0.0, "pending", Timestamp.now(), Timestamp.now())
}