package com.example.shopapp.ui.user.order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PaymentReturnActivity : AppCompatActivity() {

    private lateinit var orderId: String
    private lateinit var payerId: String
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy orderId từ SharedPreferences
        orderId = getSharedPreferences("PayPalPrefs", MODE_PRIVATE)
            .getString("order_id", "") ?: run {
            Log.e("PaymentReturnActivity", "orderId is null")
            onPaymentFailed("orderId is missing")
            return
        }

        // Lấy payerId từ SharedPreferences
        payerId = getSharedPreferences("PayPalPrefs", MODE_PRIVATE)
            .getString("payer_id", "") ?: run {
            Log.e("PaymentReturnActivity", "payerId is null")
            onPaymentFailed("payerId is missing")
            return
        }

        // Lấy returnUrl từ Intent data
        val returnUrl = intent.data?.toString()
        Log.d("PaymentReturnActivity", "Received returnUrl: $returnUrl")

        if (returnUrl != null) {
            if (returnUrl.contains("code=")) {
                val code = returnUrl.substringAfter("code=").substringBefore("&")
                Log.d("PaymentReturnActivity", "Approval code: $code")
                checkOrderStatusAndCapture(orderId, payerId)
            } else {
                Log.d("PaymentReturnActivity", "Payment canceled by user or timeout")
                onPaymentCanceled()
            }
        } else {
            Log.e("PaymentReturnActivity", "returnUrl is null")
            onPaymentFailed("returnUrl is missing")
        }

        // Cleanup SharedPreferences to prevent stale data
        getSharedPreferences("PayPalPrefs", MODE_PRIVATE).edit()
            .remove ("“order_id")
            .remove("payer_id")
            .apply()
    }

    private fun checkOrderStatusAndCapture(orderId: String, payerId: String) {
        val accessToken = getAccessToken()
        if (accessToken.isEmpty()) {
            Log.e("PaymentReturnActivity", "Access token is empty")
            onPaymentFailed("Access token is missing")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            // Kiểm tra trạng thái đơn hàng
            val status = withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderId")
                    .header("Authorization", "Bearer $accessToken")
                    .header("Content-Type", "application/json")
                    .build()

                try {
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d("PaymentReturnActivity", "Order status response: $responseBody")
                        val jsonResponse = JSONObject(responseBody)
                        jsonResponse.getString("status")
                    } else {
                        Log.e("PaymentReturnActivity", "Failed to get order status: ${response.code}, ${response.body?.string()}")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("PaymentReturnActivity", "Error checking order status", e)
                    null
                }
            }

            if (status == null) {
                onPaymentFailed("Failed to check order status")
                return@launch
            }

            if (status == "APPROVED") {
                captureOrder(orderId, payerId)
            } else {
                Log.e("PaymentReturnActivity", "Order not approved. Status: $status")
                onPaymentFailed("Order not approved by payer. Status: $status")
            }
        }
    }

    private fun captureOrder(orderId: String, payerId: String) {
        val accessToken = getAccessToken()
        if (accessToken.isEmpty()) {
            Log.e("PaymentReturnActivity", "Access token is empty")
            onPaymentFailed("Access token is missing")
            return
        }

        // Thêm payerId vào payload
        val payload = """
            {
                "payer_id": "$payerId"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderId/capture")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        // Sử dụng Coroutine để gọi API trên luồng nền
        CoroutineScope(Dispatchers.Main).launch {
            var attempts = 0
            val maxAttempts = 3
            while (attempts < maxAttempts) {
                try {
                    // Chuyển sang luồng nền để thực hiện gọi API
                    val response = withContext(Dispatchers.IO) {
                        okHttpClient.newCall(request).execute()
                    }
                    Log.d("PaymentReturnActivity", "Capture response code: ${response.code}")
                    val responseBody = response.body?.string()
                    Log.d("PaymentReturnActivity", "Capture response: $responseBody")
                    if (response.isSuccessful) {
                        onPaymentSuccess()
                        return@launch
                    } else {
                        onPaymentFailed("Capture failed: ${response.code}, $responseBody")
                        return@launch
                    }
                } catch (e: Exception) {
                    attempts++
                    Log.e("PaymentReturnActivity", "Capture error (attempt $attempts/$maxAttempts): ${e.message ?: e.toString()}")
                    if (attempts == maxAttempts) {
                        onPaymentFailed("Network error after $maxAttempts attempts: ${e.message ?: e.toString()}")
                        return@launch
                    }
                    // Đợi 1 giây trước khi thử lại
                    delay(1000)
                }
            }
        }
    }

    private fun getAccessToken(): String {
        val sharedPreferences = getSharedPreferences("PayPalPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("access_token", "") ?: ""
    }

    private fun onPaymentSuccess() {
        setResult(RESULT_OK)
        finish()
    }

    private fun onPaymentFailed(error: String) {
        val intent = Intent().putExtra("error", error)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    private fun onPaymentCanceled() {
        setResult(RESULT_CANCELED)
        finish()
    }
}