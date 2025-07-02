package com.example.shopapp.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopapp.data.model.CartItemFirebase
import com.example.shopapp.data.repository.PaymentRepository
import com.google.gson.Gson
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutClient
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutListener
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutRequest
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PaymentViewModel(
    private val application: Application,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val clientId = "Aci6ZUxwIrQ5Hz2o8Hz2sx6TlZy0dzr6US7GIZgYqnNlvQ3EJa57SRCoo0bN-TisFut0GQ5p_DpyqZ2j"
    private val returnUrl = "nativexo://paypalpay"
    private val environment = com.paypal.android.corepayments.Environment.SANDBOX

    private val coreConfig = CoreConfig(clientId, environment)
    private val payPalNativeClient = PayPalNativeCheckoutClient(
        application = application,
        coreConfig = coreConfig,
        returnUrl = returnUrl
    )

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val _paymentStatus = MutableStateFlow<PaymentStatus>(PaymentStatus.Idle)
    val paymentStatus: StateFlow<PaymentStatus> = _paymentStatus

    private var pendingOrder: PendingOrder? = null

    private val prefs = application.getSharedPreferences("PaymentPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutStart() {
                Log.d("PaymentViewModel", "Starting PayPal payment")
                _paymentStatus.value = PaymentStatus.Loading
                viewModelScope.launch {
                    delay(180_000)
                    if (_paymentStatus.value == PaymentStatus.Loading) {
                        Log.d("PaymentViewModel", "PayPal payment timed out")
                        _paymentStatus.value = PaymentStatus.Failure(Exception("PayPal payment timed out"))
                    }
                }
            }

            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
                Log.d("PaymentViewModel", "PayPal payment successful: $result")
                val orderId = prefs.getString("order_id", "")
                if (orderId.isNullOrEmpty()) {
                    _paymentStatus.value = PaymentStatus.Failure(Exception("Missing orderId"))
                    return
                }

                viewModelScope.launch {
                    val accessToken = prefs.getString("access_token", "")
                    if (accessToken.isNullOrEmpty()) {
                        _paymentStatus.value = PaymentStatus.Failure(Exception("Missing access token"))
                        return@launch
                    }

                    val (status, payerId) = withContext(Dispatchers.IO) {
                        val request = Request.Builder()
                            .url("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderId")
                            .header("Authorization", "Bearer $accessToken")
                            .header("Content-Type", "application/json")
                            .build()

                        try {
                            val response = okHttpClient.newCall(request).execute()
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string()
                                Log.d("PaymentViewModel", "Order status response: $responseBody")
                                val jsonResponse = JSONObject(responseBody)
                                val status = jsonResponse.getString("status")
                                val payerId = if (jsonResponse.has("payer")) {
                                    jsonResponse.getJSONObject("payer").getString("payer_id")
                                } else {
                                    null
                                }
                                Pair(status, payerId)
                            } else {
                                Log.e("PaymentViewModel", "Failed to get order status: ${response.code}, ${response.body?.string()}")
                                Pair(null, null)
                            }
                        } catch (e: Exception) {
                            Log.e("PaymentViewModel", "Error checking order status", e)
                            Pair(null, null)
                        }
                    }

                    if (status == null || payerId == null) {
                        _paymentStatus.value = PaymentStatus.Failure(Exception("Failed to get order status or payerId"))
                        return@launch
                    }

                    if (status != "APPROVED") {
                        _paymentStatus.value = PaymentStatus.Failure(Exception("Order not approved. Status: $status"))
                        return@launch
                    }

                    prefs.edit()
                        .putString("payer_id", payerId)
                        .apply()

                    _paymentStatus.value = PaymentStatus.Success(result)
                }
            }

            override fun onPayPalCheckoutFailure(error: PayPalSDKError) {
                Log.e("PaymentViewModel", "PayPal payment failed: ${error.message}", error)
                _paymentStatus.value = PaymentStatus.Failure(Exception(error.message ?: "PayPal payment error"))
            }

            override fun onPayPalCheckoutCanceled() {
                Log.d("PaymentViewModel", "PayPal payment canceled")
                _paymentStatus.value = PaymentStatus.Canceled
            }
        }

        restorePaymentState()
    }

    private fun restorePaymentState() {
        val pendingOrderJson = prefs.getString("pending_order", null)
        if (pendingOrderJson != null) {
            pendingOrder = gson.fromJson(pendingOrderJson, PendingOrder::class.java)
            Log.d("PaymentViewModel", "Restored pending order: $pendingOrder")
        }
    }

    private fun savePaymentState() {
        val editor = prefs.edit()
        if (pendingOrder != null) {
            val pendingOrderJson = gson.toJson(pendingOrder)
            editor.putString("pending_order", pendingOrderJson)
        } else {
            editor.remove("pending_order")
        }
        editor.apply()
        Log.d("PaymentViewModel", "Saved payment state: pendingOrder=$pendingOrder")
    }

    // Phương thức công khai để thiết lập pendingOrder
    fun setPendingOrder(order: PendingOrder) {
        pendingOrder = order
        savePaymentState()
    }

    // Phương thức công khai để gọi savePaymentState()
    fun saveState() {
        savePaymentState()
    }

    fun initiatePayPalPayment(amount: Double, currency: String) {
        viewModelScope.launch {
            Log.d("PaymentViewModel", "Initiating PayPal payment with amount: $amount $currency")
            _paymentStatus.value = PaymentStatus.Loading

            try {
                val orderId = withContext(Dispatchers.IO) {
                    paymentRepository.createOrder(amount, currency)
                }
                if (orderId.isNotEmpty()) {
                    Log.d("PaymentViewModel", "Created order with ID: $orderId")
                    prefs.edit().putString("order_id", orderId).apply()
                    startPayPalCheckout(orderId)
                } else {
                    Log.e("PaymentViewModel", "Failed to create order: Empty order ID")
                    _paymentStatus.value = PaymentStatus.Failure(Exception("Failed to create order: Empty order ID"))
                }
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error initiating PayPal payment", e)
                _paymentStatus.value = PaymentStatus.Failure(Exception(e.message ?: "Unknown error during PayPal payment initiation"))
            }
        }
    }

    private fun startPayPalCheckout(orderId: String) {
        Log.d("PaymentViewModel", "Starting PayPal checkout with order ID: $orderId")
        val request = PayPalNativeCheckoutRequest(orderId)
        try {
            payPalNativeClient.startCheckout(request)
        } catch (e: Exception) {
            Log.e("PaymentViewModel", "PayPal SDK startCheckout failed", e)
            _paymentStatus.value = PaymentStatus.Failure(e)
        }
    }

    fun updatePaymentStatus(newStatus: PaymentStatus) {
        _paymentStatus.value = newStatus
        if (newStatus is PaymentStatus.Success || newStatus is PaymentStatus.Canceled) {
            savePaymentState()
        }
    }

    fun getPendingOrder(): PendingOrder? = pendingOrder

    fun clearPendingOrder() {
        pendingOrder = null
        savePaymentState()
    }
}

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Loading : PaymentStatus()
    data class Success(val result: PayPalNativeCheckoutResult) : PaymentStatus()
    data class Failure(val error: Throwable) : PaymentStatus()
    object Canceled : PaymentStatus()
}

data class PendingOrder(
    val deliveryUsername: String,
    val deliveryAddress: String,
    val deliveryPhone: String,
    val totalPrice: Double,
    val orderItems: List<CartItemFirebase>
)