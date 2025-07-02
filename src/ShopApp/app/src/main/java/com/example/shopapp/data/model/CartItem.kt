package com.example.shopapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: String,
    @ColumnInfo(name = "productTitle") val productTitle: String,
    @ColumnInfo(name = "productImage")val productImage: String,
    @ColumnInfo(name = "price")val price: Double,
    @ColumnInfo(name = "quantity")val quantity: Int = 1,
    @ColumnInfo(name = "timestamp")val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this(
        productId = "",
        productTitle = "",
        productImage = "",
        price = 0.0,
        quantity = 1,
        timestamp = System.currentTimeMillis()
    )
}