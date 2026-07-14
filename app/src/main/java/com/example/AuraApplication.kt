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
        
        // Clean up WebView Code Cache directories if they were created as directories, letting Chromium handle them
        try {
            val webViewCacheJs = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            val webViewCacheWasm = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (webViewCacheJs.exists() && webViewCacheJs.isDirectory) {
                webViewCacheJs.delete()
            }
            if (webViewCacheWasm.exists() && webViewCacheWasm.isDirectory) {
                webViewCacheWasm.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
