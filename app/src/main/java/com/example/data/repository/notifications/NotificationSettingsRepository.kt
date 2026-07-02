package com.example.data.repository.notifications

import android.content.Context
import android.content.SharedPreferences

class NotificationSettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    var allNotificationsEnabled: Boolean
        get() = prefs.getBoolean("all_notifications", true)
        set(value) = prefs.edit().putBoolean("all_notifications", value).apply()

    var newBooksEnabled: Boolean
        get() = prefs.getBoolean("new_books", true)
        set(value) = prefs.edit().putBoolean("new_books", value).apply()

    var newVideosEnabled: Boolean
        get() = prefs.getBoolean("new_videos", true)
        set(value) = prefs.edit().putBoolean("new_videos", value).apply()

    var newToolsEnabled: Boolean
        get() = prefs.getBoolean("new_tools", true)
        set(value) = prefs.edit().putBoolean("new_tools", value).apply()

    var updatesEnabled: Boolean
        get() = prefs.getBoolean("updates", true)
        set(value) = prefs.edit().putBoolean("updates", value).apply()

    var announcementsEnabled: Boolean
        get() = prefs.getBoolean("announcements", true)
        set(value) = prefs.edit().putBoolean("announcements", value).apply()
        
    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()

    fun isCategoryEnabled(category: String): Boolean {
        if (!allNotificationsEnabled) return false
        return when (category) {
            "New Books" -> newBooksEnabled
            "New Videos" -> newVideosEnabled
            "New Tools" -> newToolsEnabled
            "App Updates" -> updatesEnabled
            "Announcements" -> announcementsEnabled
            else -> true
        }
    }
}
