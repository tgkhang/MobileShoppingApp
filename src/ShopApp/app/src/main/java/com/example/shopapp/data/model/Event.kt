package com.example.shopapp.data.model

import com.google.firebase.Timestamp

data class Event(
    val eventId: String,
    val eventType: String,
    val title: String,
    val description: String,
    val discountType: String,
    val discountValue: Double,
    val startDate: Timestamp,
    val endDate: Timestamp,
    val applicableProducts: List<String>?,
    val minPurchase: Double,
    val maxDiscount: Double,
    val applicableUsers: List<String>?,
    val usageLimit: Int,
    val status: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
) {
    constructor() : this(
        "",
        "voucher",  // or "flash sale"
        "",
        "",
        "percentage", // or "fixed amount"
        0.0,
        Timestamp.now(),
        Timestamp.now(),
        null,
        0.0,
        0.0,
        null,
        0,
        "inactive",
        Timestamp.now(),
        Timestamp.now()
    )
}