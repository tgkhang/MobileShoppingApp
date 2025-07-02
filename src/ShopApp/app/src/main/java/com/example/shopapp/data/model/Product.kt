package com.example.shopapp.data.model

import com.google.firebase.Timestamp

data class Product(
    val productId: String,
    val title: String,
    val image: String,
    val images: List<String> = emptyList(),
    val price: Double,
    val description: String,
    val brand: String,
    val model: String,
    val color: String,
    val category: String,
    val popular: Boolean,
    val discount: Double,
    val stock: Int,
    val sales: Int,
    val status: String,
    val review: List<Review>,
    val createdAt: Timestamp?,
    val updatedAt: Timestamp?
) {
    constructor() : this("", "", "",emptyList(), 0.0, "", "", "", "", "", false, 0.0, 0, 0, "", emptyList(), Timestamp.now(), Timestamp.now())
}