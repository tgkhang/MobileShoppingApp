package com.example.shopapp.data.repository

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.shopapp.data.model.User
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.Dispatchers
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val callbackManager: CallbackManager
)
{
    //tag for error log
    private val TAG = "AuthRepository"

    private val MAX_RETRIES = 4
    private val BASE_DELAY_MS = 2000L // Initial delay of 1 second
    private val MAX_TIMEOUT_MS = 20000L // 20 second timeout for operations

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun <T> executeWithRetry(
        context: Context? = null,
        operationName: String,
        operation: suspend () -> T
    ): Result<T> {
        // Check network availability if context is provided
        if (context != null && !isNetworkAvailable(context)) {
            return Result.failure(NetworkException("No network connection available. Please check your connection and try again."))
        }

        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRIES) {
            try {
                // Execute the operation with a timeout
                return withContext(Dispatchers.IO) {
                    withTimeout(MAX_TIMEOUT_MS) {
                        val result = operation()
                        Result.success(result)
                    }
                }
            } catch (e: CancellationException) {
                // Don't catch cancellation exceptions (including timeout)
                throw e
            } catch (e: UserCancellationException) {
                // Don't retry on user cancellation
                Log.d(TAG, "User cancelled operation: ${e.message}")
                return Result.failure(e)
            } catch (e: Exception) {
                lastException = when (e) {
                    is TimeoutException -> NetworkException("Operation timed out. The network connection may be slow or unstable.")
                    is UnknownHostException -> NetworkException("Cannot reach server. Please check your internet connection.")
                    else -> e
                }

                Log.e(TAG, "$operationName attempt $attempt failed: ${e.message}")

                // If this is the last attempt, don't delay, just return the failure
                if (attempt == MAX_RETRIES) {
                    break
                }

                // Exponential backoff: 1s, 2s, 4s, etc.
                val delayMs = BASE_DELAY_MS * (1 shl (attempt - 1))
                delay(delayMs)
            }
        }

        val errorMessage = when (lastException) {
            is NetworkException -> lastException.message
            else -> "Operation failed after $MAX_RETRIES attempts. Please try again later."
        }

        return Result.failure(lastException ?: Exception(errorMessage))
    }
    /**
     * Custom exception class for network-related errors
     */
    class NetworkException(message: String) : Exception(message)

    //sign up with email
    suspend fun signUp(context: Context, username: String, email: String, phone: String, password: String,address: String): Result<Unit> {
        return executeWithRetry(context, "Sign up") {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            val userData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "email" to email,
                "password" to password, //??
                "address" to "123 mainstreet",
                "phone" to phone,
                "address" to address,
                "role" to "user",
                "status" to "active",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            db.collection("users").document(userId).set(userData).await()
            Unit
        }
    }

//    suspend fun signIn(email: String, password: String): Result<String> {
//        return try {
//            val result = auth.signInWithEmailAndPassword(email, password).await()
//            val user = result.user
//            if (user != null) {
//                val role = getUserRole(user.uid)
//                Result.success(role)
//            } else {
//                Result.failure(Exception("Sign in failed"))
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error during sign in: ${e.message}")
//            Result.failure(e)
//        }
//    }
    suspend fun signIn(context: Context, email: String, password: String): Result<String> {
        return executeWithRetry(context, "Sign in") {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign in failed")
            getUserRole(user.uid)
        }
    }

    private suspend fun getUserRole(userId: String): String {
        return executeWithRetry(operationName = "Get user role") {
            val userDoc = db.collection("users").document(userId).get().await()
            userDoc.getString("role") ?: "user"
        }.getOrDefault("user")
    }
//    private suspend fun getUserRole(userId: String): String {
//        return try {
//            val userDoc = db.collection("users").document(userId).get().await()
//            userDoc.getString("role") ?: "user"
//        } catch (e: Exception) {
//            Log.e(TAG, "Error getting user role: ${e.message}")
//            "user"  // Default to user role if error
//        }
//    }

    // Google Sign-In
//    suspend fun signInWithGoogle(
//        context: Context,
//        credentialManager: androidx.credentials.CredentialManager,
//        request: GetCredentialRequest
//    ): Result<String> {
//        return try {
//            // Launch credential manager UI
//            val result = credentialManager.getCredential(
//                context = context,
//                request = request
//            )
//
//            // Process the credential
//            val credential = result.credential
//            if (credential is CustomCredential &&
//                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
//
//                // Create Google ID Token
//                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
//
//                // Firebase auth with Google
//                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
//                val authResult = auth.signInWithCredential(firebaseCredential).await()
//
//                val user = authResult.user
//                if (user != null) {
//                    // Check if this is a new user
//                    val userDoc = db.collection("users").document(user.uid).get().await()
//
//                    if (!userDoc.exists()) {
//
//                        val userData = hashMapOf(
//                            "username" to (user.displayName ?: "User"),
//                            "email" to (user.email ?: ""),
//                            "address" to "123 mainstreet",
//                            "phone" to "12345",
//                            "status" to "active",
//                            "createdAt" to FieldValue.serverTimestamp(),
//                            "updatedAt" to FieldValue.serverTimestamp(),
//                            "role" to "user"  // Default role
//                        )
//                        db.collection("users").document(user.uid).set(userData).await()
//                    }
//
//                    // Get user role
//                    val role = getUserRole(user.uid)
//                    Result.success(role)
//                } else {
//                    Result.failure(Exception("Google sign in failed"))
//                }
//            } else {
//                Result.failure(Exception("Not a Google credential"))
//            }
//        } catch (e: GetCredentialException) {
//            Log.e(TAG, "Error getting credential: ${e.message}")
//            Result.failure(e)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error during Google sign in: ${e.message}")
//            Result.failure(e)
//        }
//    }

    suspend fun resetPassword(context: Context, email: String)  {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        request: GetCredentialRequest
    ): Result<String> {
        return executeWithRetry(context, "Google sign in") {
            try {
                // Launch credential manager UI
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                // Process the credential
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    // Create Google ID Token
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    // Firebase auth with Google
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()

                    val user = authResult.user ?: throw Exception("Google sign in failed")

                    // Check if this is a new user
                    val userDoc = db.collection("users").document(user.uid).get().await()

                    if (!userDoc.exists()) {
                        val userData = hashMapOf(
                            "userId" to user.uid,
                            "username" to (user.displayName ?: "User"),
                            "email" to (user.email ?: ""),
                            "address" to "123 mainstreet",
                            "phone" to "12345",
                            "status" to "active",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp(),
                            "role" to "user"  // Default role
                        )
                        db.collection("users").document(user.uid).set(userData).await()
                    }

                    // Get user role
                    getUserRole(user.uid)
                } else {
                    throw Exception("Not a Google credential")
                }
            } catch (e: GetCredentialException) {
                // Handle credential exceptions (including user cancellation)
                Log.d(TAG, "Google sign-in canceled or failed: ${e.message}")
                throw UserCancellationException("Sign-in was canceled")
            }
        }
    }

    // Custom exception for user cancellation
    class UserCancellationException(message: String) : Exception(message)

    suspend fun signInWithFacebook(context: Context): Result<String> {
        if (context !is Activity) {
            return Result.failure(Exception("Facebook login requires an Activity context"))
        }

        val loginManager = LoginManager.getInstance()
        val deferred = CompletableDeferred<Result<String>>()

        Log.d(TAG, "Starting Facebook sign-in process")

        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "Facebook login success - Access token received")

                val accessToken = result.accessToken
                val credential = FacebookAuthProvider.getCredential(accessToken.token)

                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Firebase auth with Facebook credential successful")
                            val user = auth.currentUser
                            if (user != null) {
                                Log.d(TAG, "User authenticated: ${user.uid}")
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { document ->
                                        Log.d(TAG, "Firestore user document fetch success")
                                        if (!document.exists()) {
                                            val userData = hashMapOf(
                                                "userId" to user.uid,
                                                "username" to (user.displayName ?: "User"),
                                                "email" to (user.email ?: ""),
                                                "phone" to (user.phoneNumber ?: ""),
                                                "address" to "123 mainstreet",
                                                "status" to "active",
                                                "createdAt" to FieldValue.serverTimestamp(),
                                                "updatedAt" to FieldValue.serverTimestamp(),
                                                "role" to "user"
                                            )
                                            db.collection("users").document(user.uid).set(userData)
                                                .addOnSuccessListener {
                                                    Log.d(TAG, "Firestore user data set success")
                                                    deferred.complete(Result.success(getUserRoleBlocking(user.uid)))
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(TAG, "Failed to set user data: ${e.message}", e)
                                                    deferred.complete(Result.failure(e))
                                                }
                                        } else {
                                            Log.d(TAG, "User already exists in Firestore")
                                            deferred.complete(Result.success(getUserRoleBlocking(user.uid)))
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Failed to fetch user document: ${e.message}", e)
                                        deferred.complete(Result.failure(e))
                                    }
                            } else {
                                Log.e(TAG, "Firebase auth succeeded but user is null")
                                deferred.complete(Result.failure(Exception("Facebook sign in failed: No user")))
                            }
                        } else {
                            Log.e(TAG, "Firebase auth failed: ${task.exception?.message}", task.exception)
                            deferred.complete(Result.failure(task.exception ?: Exception("Facebook sign in failed")))
                        }
                    }
            }

            override fun onCancel() {
                Log.d(TAG, "Facebook login cancelled")
                deferred.complete(Result.failure(Exception("Facebook login cancelled")))
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "Facebook login error: ${error.message}", error)
                deferred.complete(Result.failure(error))
            }
        })

        withContext(Dispatchers.Main) {
            Log.d(TAG, "Triggering Facebook login UI")
            loginManager.logInWithReadPermissions(context, listOf("email", "public_profile"))
        }

        return deferred.await()
    }

    // Blocking version of getUserRole for use in callbacks (avoid suspending in callbacks)
    private fun getUserRoleBlocking(userId:String): String{
        return try {
            val userDoc= db.collection("users").document(userId).get().result
            userDoc?.getString("role") ?: "user"
        }catch (e: Exception){
            "user"
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            userDoc.toObject(User::class.java)?.copy(userId = userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID: ${e.message}")
            null
        }
    }
}