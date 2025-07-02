package com.example.shopapp.data.model

data class CartItemFirebase(
    val productId: String = "",
    val productTitle: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)