package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

import android.content.Context

object AppContext {
    lateinit var context: Context
}

class AuraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.context = this
        if (FirebaseApp.getApps(this).isEmpty()) {
            val hasCustomKey = BuildConfig.FIREBASE_API_KEY.isNotEmpty() && BuildConfig.FIREBASE_API_KEY != "YOUR_API_KEY"
            
            val apiKey = if (hasCustomKey) BuildConfig.FIREBASE_API_KEY else "AIzaSyAPfjTTxM5Nrjw1raxSjASdgOLNZFoJKQA"
            
            // Allow parsing Project ID from Auth Domain if provided
            val authDomain = BuildConfig.FIREBASE_AUTH_DOMAIN
            val extractedProjectId = if (authDomain.isNotEmpty() && authDomain != "YOUR_AUTH_DOMAIN") {
                authDomain.replace(".firebaseapp.com", "")
            } else "aura-hub-86621"
            
            val projectId = if (BuildConfig.FIREBASE_PROJECT_ID.isNotEmpty() && BuildConfig.FIREBASE_PROJECT_ID != "YOUR_PROJECT_ID") {
                BuildConfig.FIREBASE_PROJECT_ID
            } else extractedProjectId

            val appId = if (BuildConfig.FIREBASE_APP_ID.isNotEmpty() && BuildConfig.FIREBASE_APP_ID != "YOUR_APP_ID") {
                BuildConfig.FIREBASE_APP_ID
            } else "1:1096686366523:web:f06c054552fb8c12a3111e" // If they don't provide appId, this might cause issues, but we do our best

            val databaseUrl = if (BuildConfig.FIREBASE_DATABASE_URL.isNotEmpty() && BuildConfig.FIREBASE_DATABASE_URL != "YOUR_DATABASE_URL") {
                BuildConfig.FIREBASE_DATABASE_URL
            } else "https://$projectId-default-rtdb.firebaseio.com"

            val storageBucket = if (BuildConfig.FIREBASE_STORAGE_BUCKET.isNotEmpty() && BuildConfig.FIREBASE_STORAGE_BUCKET != "YOUR_STORAGE_BUCKET") {
                BuildConfig.FIREBASE_STORAGE_BUCKET
            } else "$projectId.firebasestorage.app"

            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setApplicationId(appId)
                .setDatabaseUrl(databaseUrl)
                .setProjectId(projectId)
                .setStorageBucket(storageBucket)
                // .setAuthDomain(authDomain) // Need to check if available
                .build()
            FirebaseApp.initializeApp(this, options)
        }
    }
}
