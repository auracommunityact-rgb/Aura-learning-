package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp

object AppContext {
    lateinit var context: Context
}

class AuraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.context = this
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize Google Mobile Ads SDK and cache interstitial
        try {
            com.example.utils.AdMobManager.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Register Notification Channels
        com.example.utils.NotificationHelper.registerNotificationChannels(this)
        
        
        // Clean up WebView Code Cache directories recursively
        try {
            val codeCacheDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            if (codeCacheDir.exists()) {
                codeCacheDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
