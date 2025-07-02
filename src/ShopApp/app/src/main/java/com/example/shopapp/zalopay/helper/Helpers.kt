package com.example.shopapp.zalopay.helper

import android.annotation.SuppressLint
import com.example.shopapp.zalopay.helper.HMac.HMacUtil
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date

object Helpers {
    private var transIdDefault = 1

    @get:SuppressLint("DefaultLocale")
    val appTransId: String
        get() {
            if (transIdDefault >= 100000) {
                transIdDefault = 1
            }

            transIdDefault += 1
            @SuppressLint("SimpleDateFormat") val formatDateTime = SimpleDateFormat("yyMMdd_hhmmss")
            val timeString = formatDateTime.format(Date())
            return String.format("%s%06d", timeString, transIdDefault)
        }

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun getMac(key: String, data: String): String {
        val result = HMacUtil.HMacHexStringEncode(
            HMacUtil.HMACSHA256,
            key,
            data
        )
        return result ?: throw IllegalStateException("HMAC computation failed, result is null")
    }
}