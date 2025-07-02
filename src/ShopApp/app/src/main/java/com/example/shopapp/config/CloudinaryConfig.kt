package com.example.shopapp.config

import android.content.Context
import java.util.Properties
import com.cloudinary.android.MediaManager

object CloudinaryConfig {
    fun init(context: Context) {
        val properties = Properties()
        val inputStream = context.assets.open("cloudinary.properties")
        properties.load(inputStream)

        val config = mapOf(
            "cloud_name" to properties.getProperty("cloud_name"),
            "api_key" to properties.getProperty("api_key"),
            "api_secret" to properties.getProperty("api_secret")
        )
        MediaManager.init(context, config)
    }
}
