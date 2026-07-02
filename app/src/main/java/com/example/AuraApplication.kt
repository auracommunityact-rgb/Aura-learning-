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
    }
}
