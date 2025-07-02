package com.example.shopapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shopapp.data.repository.UserKRepository
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModelFactory(
    private val userRepository: UserKRepository,
    private val auth: FirebaseAuth ,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository, auth, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}