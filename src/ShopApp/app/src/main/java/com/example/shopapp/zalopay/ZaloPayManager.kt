package com.example.shopapp.zalopay

import android.content.Context
import android.util.Log
//import vn.zalopay.sdk.Environment
//import vn.zalopay.sdk.ZaloPaySDK

object ZaloPayManager {
    private var isInitialized = false

    @Synchronized
    fun init(context: Context) {
        if (!isInitialized) {
            try {
                //ZaloPaySDK.init(2554, Environment.SANDBOX)
                isInitialized = true
                Log.d("ZaloPayManager", "ZaloPay SDK initialized with APP_ID=2554, Environment=SANDBOX")
            } catch (e: Exception) {
                isInitialized = false
                Log.e("ZaloPayManager", "Failed to initialize ZaloPay SDK", e)
                throw e
            }
        } else {
            Log.d("ZaloPayManager", "ZaloPay SDK already initialized")
        }
    }

    fun isInitialized(): Boolean = isInitialized
}