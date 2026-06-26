package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class AuraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            val apiKey = if (BuildConfig.FIREBASE_API_KEY.isNotEmpty() && BuildConfig.FIREBASE_API_KEY != "YOUR_API_KEY") {
                BuildConfig.FIREBASE_API_KEY
            } else {
                "AIzaSyAPfjTTxM5Nrjw1raxSjASdgOLNZFoJKQA" // Fallback to provided key for seamless demo/prototype use
            }
            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setApplicationId("1:1096686366523:web:f06c054552fb8c12a3111e")
                .setDatabaseUrl("https://aura-hub-86621-default-rtdb.firebaseio.com")
                .setProjectId("aura-hub-86621")
                .setStorageBucket("aura-hub-86621.firebasestorage.app")
                .setGcmSenderId("1096686366523")
                .build()
            FirebaseApp.initializeApp(this, options)
        }
    }
}
