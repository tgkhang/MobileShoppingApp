package com.example.shopapp.data.model

import com.google.firebase.Timestamp

data class User(
        val userId: String,
        val username: String,
        val email: String,
        val address: String,
        val phone: String,
        val role: String,
        val status: String,
        val createdAt: Timestamp?,
        val updatedAt: Timestamp?
) {
        constructor() : this("", "", "", "", "", "user", "active", Timestamp.now(), Timestamp.now())
}