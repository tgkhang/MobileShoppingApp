package com.example.shopapp.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PaymentRepository(private val context: Context) {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson = Gson()

    suspend fun createOrder(amount: Double, currency: String): String = withContext(Dispatchers.IO) {
        val accessToken = createAccessToken()
        val formattedAmount = String.format("%.2f", amount)
        val requestBody = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().put(JSONObject().apply {
                put("amount", JSONObject().apply {
                    put("currency_code", currency)
                    put("value", formattedAmount)
                })
            }))
        }.toString()

        val request = Request.Builder()
            .url("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .header("Authorization", "Bearer $accessToken")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        Log.d("PaymentRepository", "PayPal createOrder response: code=${response.code}, body=$responseBody")

        if (response.isSuccessful) {
            val json = JSONObject(responseBody)
            val orderId = json.optString("id", "")
            if (orderId.isEmpty()) {
                Log.e("PaymentRepository", "PayPal order created but order ID is empty. Full response: $responseBody")
            }
            orderId
        } else {
            throw Exception("Failed to create PayPal order: code=${response.code}, body=$responseBody")
        }
    }

    private suspend fun createAccessToken(): String = withContext(Dispatchers.IO) {
        val clientId = "Aci6ZUxwIrQ5Hz2o8Hz2sx6TlZy0dzr6US7GIZgYqnNlvQ3EJa57SRCoo0bN-TisFut0GQ5p_DpyqZ2j"
        val clientSecret = "EH0EKRqY-5mwgZZ_QF_ZiNc2L3_jUWONWgH-dkPyKVC9qXQMD8RBP1OuyOGuWSiBmSS7jsUN6U-6-xDZ"

        val authString = "$clientId:$clientSecret"
        val authEncoded = android.util.Base64.encodeToString(authString.toByteArray(), android.util.Base64.NO_WRAP)

        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val request = Request.Builder()
            .url("https://api-m.sandbox.paypal.com/v1/oauth2/token")
            .header("Authorization", "Basic $authEncoded")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        Log.d("PaymentRepository", "PayPal access token response: code=${response.code}, body=$responseBody")

        if (response.isSuccessful) {
            val accessJson = gson.fromJson(responseBody, Map::class.java)
            val accessToken = accessJson["access_token"] as String

            context.getSharedPreferences("PayPalPrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("access_token", accessToken)
                .apply()

            accessToken
        } else {
            throw Exception("Failed to create PayPal access token: code=${response.code}, body=$responseBody")
        }
    }

    private suspend fun createPaypalOrder(amount: Double, currency: String, accessToken: String): String = withContext(Dispatchers.IO) {
        val formattedAmount = String.format("%.2f", amount)
        val orderData = """
            {
                "intent": "CAPTURE",
                "purchase_units": [
                    {
                        "amount": {
                            "currency_code": "$currency",
                            "value": "$formattedAmount"
                        }
                    }
                ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .post(orderData.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        Log.d("PaymentRepository", "PayPal createPaypalOrder response: code=${response.code}, body=$responseBody")

        if (response.isSuccessful) {
            val json = JSONObject(responseBody)
            val orderId = json.optString("id", "")
            if (orderId.isEmpty()) {
                Log.e("PaymentRepository", "PayPal order created but order ID is empty. Full response: $responseBody")
            }
            orderId
        } else {
            throw Exception("Failed to create PayPal order: code=${response.code}, body=$responseBody")
        }
    }
}