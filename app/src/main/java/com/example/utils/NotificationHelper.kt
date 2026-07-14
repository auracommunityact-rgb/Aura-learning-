package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_STUDY_ALARMS = "study_alarms"
    const val CHANNEL_NEW_BOOKS = "new_books"
    const val CHANNEL_NEW_VIDEOS = "new_videos"
    const val CHANNEL_NEW_TOOLS = "new_tools"
    const val CHANNEL_APP_UPDATES = "app_updates"
    const val CHANNEL_ANNOUNCEMENTS = "announcements"
    const val CHANNEL_SYSTEM = "system"

    fun registerNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                val channels = listOf(
                    NotificationChannel(
                        CHANNEL_STUDY_ALARMS,
                        "Study Alarms",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Alarms for scheduled study sessions"
                        enableVibration(true)
                        setShowBadge(true)
                    },
                    NotificationChannel(
                        CHANNEL_NEW_BOOKS,
                        "New Books",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Notify when new books are added"
                        enableVibration(true)
                        setShowBadge(true)
                    },
                    NotificationChannel(
                        CHANNEL_NEW_VIDEOS,
                        "New Videos",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Notify when new videos are uploaded"
                        enableVibration(true)
                        setShowBadge(true)
                    },
                    NotificationChannel(
                        CHANNEL_NEW_TOOLS,
                        "New Tools",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Notify about new tools"
                        enableVibration(true)
                        setShowBadge(true)
                    },
                    NotificationChannel(
                        CHANNEL_APP_UPDATES,
                        "App Updates",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Notify about application updates"
                        enableVibration(true)
                        setShowBadge(true)
                    },
                    NotificationChannel(
                        CHANNEL_ANNOUNCEMENTS,
                        "Announcements",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Important announcements and news"
                        enableVibration(true)
                        setShowBadge(true)
                    },
                    NotificationChannel(
                        CHANNEL_SYSTEM,
                        "System",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "System notifications and status alerts"
                        enableVibration(true)
                        setShowBadge(true)
                    }
                )
                
                notificationManager.createNotificationChannels(channels)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
