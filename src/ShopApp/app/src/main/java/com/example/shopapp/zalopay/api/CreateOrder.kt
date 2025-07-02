package com.example.shopapp.zalopay.api

import android.util.Log
import com.example.shopapp.zalopay.constant.AppInfo
import com.example.shopapp.zalopay.helper.Helpers
import org.json.JSONObject
import java.util.Date

class CreateOrder {
    private inner class CreateOrderData(amount: String) {
        var AppId: String
        var AppUser: String
        var AppTime: String
        var Amount: String
        var AppTransId: String
        var EmbedData: String
        var Items: String
        var BankCode: String
        var Description: String
        var Mac: String

        init {
            val appTime = Date().time
            AppId = AppInfo.APP_ID.toString()
            AppUser = "Android_Demo"
            AppTime = appTime.toString()
            Amount = amount.trim()
            AppTransId = Helpers.appTransId
            EmbedData = "{}"
            Items = "[]"
            BankCode = "zalopayapp"
            Description = "Order payment #$AppTransId"
            val inputHMac = String.format(
                "%s|%s|%s|%s|%s|%s|%s",
                AppId,
                AppTransId,
                AppUser,
                Amount,
                AppTime,
                EmbedData,
                Items
            )
            Log.d("CreateOrder", "HMAC input: $inputHMac")
            Mac = Helpers.getMac(AppInfo.MAC_KEY, inputHMac)
            Log.d("CreateOrder", "HMAC output: $Mac")
        }
    }

    @Throws(Exception::class)
    fun createOrder(amount: String): JSONObject? {
        val input = CreateOrderData(amount)

        val params = JSONObject().apply {
            put("app_id", input.AppId.toInt())
            put("app_user", input.AppUser)
            put("app_time", input.AppTime.toLong())
            put("amount", input.Amount.toLong())
            put("app_trans_id", input.AppTransId)
            put("embed_data", input.EmbedData)
            put("item", input.Items)
            put("bank_code", input.BankCode)
            put("description", input.Description)
            put("mac", input.Mac)
            put("currency", "VND")
        }

        Log.d("CreateOrder", "Request params: $params")
        val response = HttpProvider.sendPost(AppInfo.URL_CREATE_ORDER, params)
        Log.d("CreateOrder", "Response: $response")
        return response
    }
}