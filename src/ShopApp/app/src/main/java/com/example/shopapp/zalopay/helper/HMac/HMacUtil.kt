package com.example.shopapp.zalopay.helper.HMac

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.shopapp.zalopay.helper.HMac.HexStringUtil.byteArrayToHexString
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.LinkedList
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


object HMacUtil {
    const val HMACMD5: String = "HmacMD5"
    const val HMACSHA1: String = "HmacSHA1"
    const val HMACSHA256: String = "HmacSHA256"
    const val HMACSHA512: String = "HmacSHA512"
    val UTF8CHARSET: Charset = StandardCharsets.UTF_8

    val HMACS: LinkedList<String> = LinkedList(
        mutableListOf(
            "UnSupport",
            "HmacSHA256",
            "HmacMD5",
            "HmacSHA384",
            "HMacSHA1",
            "HmacSHA512"
        )
    )

    // @formatter:on
    private fun HMacEncode(algorithm: String, key: String, data: String): ByteArray? {
        var macGenerator: Mac? = null
        try {
            macGenerator = Mac.getInstance(algorithm)
            val signingKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), algorithm)
            macGenerator.init(signingKey)
        } catch (ex: Exception) {
        }

        if (macGenerator == null) {
            return null
        }

        var dataByte: ByteArray? = null
        try {
            dataByte = data.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
        }

        return macGenerator.doFinal(dataByte)
    }

    /**
     * Calculating a message authentication code (MAC) involving a cryptographic
     * hash function in combination with a secret cryptographic key.
     *
     * The result will be represented base64-encoded string.
     *
     * @param algorithm A cryptographic hash function (such as MD5 or SHA-1)
     *
     * @param key A secret cryptographic key
     *
     * @param data The message to be authenticated
     *
     * @return Base64-encoded HMAC String
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun HMacBase64Encode(algorithm: String, key: String, data: String): String? {
        val hmacEncodeBytes = HMacEncode(algorithm, key, data) ?: return null
        return Base64.getEncoder().encodeToString(hmacEncodeBytes)
    }

    /**
     * Calculating a message authentication code (MAC) involving a cryptographic
     * hash function in combination with a secret cryptographic key.
     *
     * The result will be represented hex string.
     *
     * @param algorithm A cryptographic hash function (such as MD5 or SHA-1)
     *
     * @param key A secret cryptographic key
     *
     * @param data The message to be authenticated
     *
     * @return Hex HMAC String
     */
    fun HMacHexStringEncode(algorithm: String, key: String, data: String): String? {
        val hmacEncodeBytes = HMacEncode(algorithm, key, data) ?: return null
        return byteArrayToHexString(hmacEncodeBytes)
    }
}
