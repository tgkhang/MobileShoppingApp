package com.example.shopapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.shopapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import androidx.credentials.GetCredentialRequest
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Address
import android.os.Build
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel()
{
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    var resetPasswordStatus by mutableStateOf<ResetPasswordStatus?>(null)
    val loginState: StateFlow<LoginState> = _loginState

    private var credentialManager: CredentialManager? = null

    // Instantiate a Google sign-in request
    val googleIdOption = GetGoogleIdOption.Builder()
        // ADD server's client ID, not your Android client ID.
        .setServerClientId("221760298186-d2s802ed09tleiq5n9a527d7ivhnvqlr.apps.googleusercontent.com")
        // Only show accounts previously used to sign in.
        .setFilterByAuthorizedAccounts(true)
        .build()

    // Create the Credential Manager request
    private val googleCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    private val _notificationPermissionGranted = MutableStateFlow(false)
    val notificationPermissionGranted: StateFlow<Boolean> = _notificationPermissionGranted

    // Initialize credential manager if needed
    private fun ensureCredentialManagerInitialized(context: Context) {
        if (credentialManager == null) {
            credentialManager = CredentialManager.create(context)
        }
    }

    fun signUp(context: Context, username:String,email: String, phone: String, password: String, address: String ) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.signUp(context,username,email,phone, password,address)
            _loginState.value = when {
                result.isSuccess -> {
                    updateFcmToken()
                    LoginState.Success("Sign Up Successful", "user") // Defaulting role to "user"
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                    LoginState.Error(errorMessage)
                }
            }
        }
    }

    fun signIn(context: Context,email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.signIn(context,email, password)
            _loginState.value = when {
                result.isSuccess -> {
                    val role = result.getOrNull() ?: "user"  // Ensure non-null role
                    updateFcmToken()
                    LoginState.Success("Sign In Successful", role)
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                    LoginState.Error(errorMessage)
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                // Initialize credential manager if needed
                ensureCredentialManagerInitialized(context)

                val manager = credentialManager ?: run {
                    _loginState.value = LoginState.Error("Failed to initialize Credential Manager")
                    return@launch
                }

                val result = authRepository.signInWithGoogle(
                    context,
                    manager,
                    googleCredentialRequest
                )

                _loginState.value = when {
                    result.isSuccess -> {
                        val role = result.getOrNull() ?: "user"
                        updateFcmToken()
                        LoginState.Success("Google Sign In Successful", role)
                    }
                    else -> {
                        val exception = result.exceptionOrNull()
                        // Handle user cancellation specially
                        if (exception is AuthRepository.UserCancellationException) {
                            LoginState.Idle  // Just return to idle state on cancellation
                        } else {
                            val errorMessage = exception?.message ?: "Unknown error"
                            LoginState.Error(errorMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                // Make sure any unexpected exceptions also reset the loading state
                Log.e("AuthViewModel", "Unexpected error in Google sign-in: ${e.message}", e)
                _loginState.value = LoginState.Error("Sign-in failed: ${e.message}")
            }
        }
    }


    fun signInWithFacebook(context: Context){
        viewModelScope.launch {
            _loginState.value= LoginState.Loading
            val result = authRepository.signInWithFacebook(context)
            _loginState.value = when {
                result.isSuccess -> {
                    val role = result.getOrNull() ?: "user"
                    updateFcmToken()
                    LoginState.Success("Facebook Sign In Successful", role)
                }
                else -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                    LoginState.Error(errorMessage)
                }
            }
        }
    }

    fun signOut(navController: NavController) {
        viewModelScope.launch {
            authRepository.signOut()
            _loginState.value = LoginState.Idle

            // Navigate to auth screen and clear back stack
            navController.navigate("auth") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetPassword(context: Context, email: String) {
        viewModelScope.launch {
            try {
                authRepository.resetPassword(context, email)
                resetPasswordStatus = ResetPasswordStatus.Success
            } catch (e: Exception) {
                resetPasswordStatus = ResetPasswordStatus.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun updateFcmToken() {
        val currentUser = auth.currentUser ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                viewModelScope.launch {
                    try {
                        db.collection("users")
                            .document(currentUser.uid)
                            .update("fcmToken", token)

                        Log.d("AuthViewModel", "FCM token updated: $token")
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Failed to update FCM token", e)
                    }
                }

            }
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun checkNotificationPermission(context: Context): Boolean {
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android < 13, notification permissions are granted by default
            true
        }

        _notificationPermissionGranted.value = permissionGranted
        return permissionGranted
    }

    sealed class ResetPasswordStatus {
        object Success : ResetPasswordStatus()
        data class Error(val message: String) : ResetPasswordStatus()
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val message: String, val role: String) : LoginState()
        data class Error(val errorMessage: String) : LoginState()
    }
}
