package com.example.shopapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shopapp.data.repository.AuthRepository
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModelFactory(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val callbackManager: CallbackManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val repository = AuthRepository(auth, db, callbackManager)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, auth, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}