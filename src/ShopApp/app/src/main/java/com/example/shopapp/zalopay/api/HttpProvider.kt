package com.example.shopapp.zalopay.api

import android.util.Log
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.TlsVersion
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object HttpProvider {
    fun sendPost(url: String?, params: JSONObject?): JSONObject? {
        var data: JSONObject? = null
        try {
            // Define the ConnectionSpec with TLS 1.2 and specific cipher suites
            val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                )
                .build()

            // Create OkHttpClient with the ConnectionSpec
            val client = OkHttpClient.Builder()
                .connectionSpecs(listOf(spec))
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .build()

            // Create the HTTP request with JSON body
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = params?.toString()?.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url!!)
                .addHeader("Content-Type", "application/json")
                .post(requestBody!!)
                .build()

            // Execute the request
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("HttpProvider", "Request failed: ${response.code}, ${response.body?.string()}")
                    data = null
                } else {
                    val responseBody = response.body?.string()
                    data = responseBody?.let { JSONObject(it) }
                    Log.d("HttpProvider", "Request successful: $data")
                }
            }
        } catch (e: IOException) {
            Log.e("HttpProvider", "IOException: ${e.message}", e)
        } catch (e: JSONException) {
            Log.e("HttpProvider", "JSONException: ${e.message}", e)
        }

        return data
    }
}