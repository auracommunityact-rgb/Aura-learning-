package com.example

import android.app.Application
import android.content.Context

object AppContext {
    lateinit var context: Context
}

class AuraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.context = this
    }
}
