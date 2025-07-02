package com.example.shopapp.services

import android.content.Context
import android.util.Log
import com.example.shopapp.data.model.Order
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class NotificationService(
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    private val client = OkHttpClient()
    private val fcmUrl = "https://fcm.googleapis.com/v1/projects/shopandroid-86863/messages:send"
    private val scope = "https://www.googleapis.com/auth/firebase.messaging"

    // Cache the access token
    private var accessToken: String? = null
    private var tokenExpiration: Long = 0

    suspend fun sendOrderStatusNotification(order: Order, userId: String) {
        try {
            // Get the FCM token for the user
            val userDoc = firestore.collection("users").document(userId)
                .get().await()

            val fcmToken = userDoc.getString("fcmToken") ?: return

            // Build the notification payload
            val title = "Order Status Updated"
            val message = "Your order #${order.orderId} is currently shipping!"

            sendNotification(fcmToken, title, message, mapOf(
                "orderId" to order.orderId,
                "orderStatus" to order.status,
                "notificationType" to "ORDER_STATUS_UPDATE"
            ))

            // Save the notification to Firestore for in-app display
            saveNotification(userId, title, message, order.orderId)

        } catch (e: Exception) {
            Log.e("NotificationService", "Failed to send notification", e)
        }
    }

    private suspend fun sendNotification(
        token: String,
        title: String,
        message: String,
        data: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()

            // Build the FCM HTTP v1 API payload
            val json = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)

                    // Notification part
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", message)
                    })

                    // Android specific configuration
                    put("android", JSONObject().apply {
                        put("priority", "high")
                        put("notification", JSONObject().apply {
                            put("sound", "default")
                            put("default_sound", true)
                            put("default_vibrate_timings", true)
                            put("default_light_settings", true)
                        })
                    })

                    // Data payload
                    if (data.isNotEmpty()) {
                        val dataJson = JSONObject()
                        data.forEach { (key, value) ->
                            dataJson.put(key, value)
                        }
                        put("data", dataJson)
                    }
                })
            }

            val requestBody = json.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(fcmUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "No response body"
                    throw IOException("Unexpected code $response: $responseBody")
                } else {
                    Log.d("NotificationService", "Notification sent successfully")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Error sending FCM notification", e)
        }
    }

    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()

        // Return cached token if it's still valid
        if (accessToken != null && currentTime < tokenExpiration - 60000) {
            return@withContext accessToken!!
        }

        try {
            // Load the service account JSON file from assets
            val serviceAccountStream: InputStream = context.assets.open("shopandroid-86863-firebase-adminsdk-fbsvc-74df904fd1.json")

            // Get access token with FCM API scope
            val credentials = GoogleCredentials
                .fromStream(serviceAccountStream)
                .createScoped(listOf(scope))

            credentials.refresh()

            accessToken = credentials.accessToken.tokenValue
            tokenExpiration = credentials.accessToken.expirationTime.time

            return@withContext accessToken!!
        } catch (e: Exception) {
            Log.e("NotificationService", "Error getting access token", e)
            throw e
        }
    }

    private suspend fun saveNotification(
        userId: String,
        title: String,
        message: String,
        orderId: String
    ) = withContext(Dispatchers.IO) {
        try {
            val notification = hashMapOf(
                "userId" to userId,
                "title" to title,
                "message" to message,
                "orderId" to orderId,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false
            )

            firestore.collection("notifications")
                .add(notification)
                .await()
        } catch (e: Exception) {
            Log.e("NotificationService", "Error saving notification", e)
        }
    }
}