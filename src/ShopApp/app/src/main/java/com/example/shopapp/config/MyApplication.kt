package com.example.shopapp.config

import android.app.Application
import android.util.Log
import com.cloudinary.android.MediaManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.util.Properties

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Offline Persistence for Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
        Log.d("MyApplication", "Offline persistence enabled for Firestore")

        // Initialize Cloudinary MediaManager
        try {
            val properties = Properties()
            val inputStream = assets.open("cloudinary.properties")
            properties.load(inputStream)

            val config = mapOf(
                "cloud_name" to properties.getProperty("cloud_name"),
                "api_key" to properties.getProperty("api_key"),
                "api_secret" to properties.getProperty("api_secret")
            )
            MediaManager.init(this, config)
            Log.d("MyApplication", "Cloudinary MediaManager initialized successfully")
        } catch (e: Exception) {
            Log.e("MyApplication", "Failed to initialize Cloudinary MediaManager", e)
        }
    }
}