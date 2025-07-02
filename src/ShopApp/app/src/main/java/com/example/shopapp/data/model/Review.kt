package com.example.shopapp.data.model

import com.google.firebase.Timestamp

data class Review (
    val reviewId: String,
    val userId: String,
    val rating: Double,
    val comment: String,
    val createdAt: Timestamp?,
    val updatedAt: Timestamp?
){
    constructor() : this("","", 0.0, "", null, null)
}