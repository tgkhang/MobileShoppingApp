package com.example.shopapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.shopapp.data.model.User
import com.example.shopapp.data.repository.UserKRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class ProfileViewModel(
    private val userRepository: UserKRepository,
    private val auth: FirebaseAuth,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val TAG = "ProfileViewModel"

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Unknown)
    val verificationState: StateFlow<VerificationState> = _verificationState

    init {
        loadUserData()
        checkEmailVerificationStatus()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _userState.value = UserState.Loading

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    val user = userRepository.getUserById(userId)

                    if (user != null) {
                        _userState.value = UserState.Success(user)
                    } else {
                        // Try to get user by email as fallback
                        val email = currentUser.email
                        if (email != null) {
                            val userByEmail = userRepository.getUserByEmail(email)
                            if (userByEmail != null) {
                                _userState.value = UserState.Success(userByEmail)
                            } else {
                                _userState.value = UserState.Error("User data not found")
                            }
                        } else {
                            _userState.value = UserState.Error("User data not found")
                        }
                    }
                } else {
                    _userState.value = UserState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun checkEmailVerificationStatus() {
        viewModelScope.launch {
            _verificationState.value = VerificationState.Loading

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Reload user to get the latest verification status
                    currentUser.reload().await()

                    if (currentUser.isEmailVerified) {
                        _verificationState.value = VerificationState.Verified
                        Log.d(TAG, "Email is verified")
                    } else {
                        _verificationState.value = VerificationState.Unverified
                        Log.d(TAG, "Email is not verified")
                    }
                } else {
                    _verificationState.value = VerificationState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Error(e.message ?: "Failed to check verification status")
                Log.e(TAG, "Error checking verification status: ${e.message}")
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            _verificationState.value = VerificationState.SendingVerification

            try {
                val user = auth.currentUser
                if (user != null) {
                    user.sendEmailVerification().await()
                    _verificationState.value = VerificationState.VerificationSent
                    Log.d(TAG, "Verification email sent")
                } else {
                    _verificationState.value = VerificationState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                _verificationState.value = VerificationState.Error(e.message ?: "Failed to send verification email")
                Log.e(TAG, "Error sending verification email: ${e.message}")
            }
        }
    }

    fun updateUserProfile(updatedUser: User) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading

            try {
                val success = userRepository.updateUser(updatedUser)
                if (success) {
                    _updateState.value = UpdateState.Success("Profile updated successfully")
                    loadUserData() // Reload user data after update
                } else {
                    _updateState.value = UpdateState.Error("Failed to update profile")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun signOut(navController: NavController) {
        authViewModel.signOut(navController)
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    fun resetVerificationState() {
        _verificationState.value = VerificationState.Unknown
    }

    sealed class UserState {
        object Loading : UserState()
        data class Success(val user: User) : UserState()
        data class Error(val errorMessage: String) : UserState()
    }

    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        data class Success(val message: String) : UpdateState()
        data class Error(val errorMessage: String) : UpdateState()
    }
    sealed class VerificationState {
        object Unknown : VerificationState()
        object Loading : VerificationState()
        object Verified : VerificationState()
        object Unverified : VerificationState()
        object SendingVerification : VerificationState()
        object VerificationSent : VerificationState()
        data class Error(val errorMessage: String) : VerificationState()
    }
}