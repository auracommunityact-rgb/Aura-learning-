package com.example

import android.app.Application
import android.content.Context
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
        
        // Pre-create WebView Code Cache directories to prevent Chromium WebView cache errors
        try {
            val webViewCacheJs = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            val webViewCacheWasm = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!webViewCacheJs.exists()) {
                webViewCacheJs.mkdirs()
            }
            if (!webViewCacheWasm.exists()) {
                webViewCacheWasm.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
