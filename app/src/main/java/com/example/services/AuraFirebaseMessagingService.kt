package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.notifications.NotificationEntity
import com.example.data.repository.notifications.NotificationRepository
import com.example.data.repository.notifications.NotificationSettingsRepository
import com.example.data.supabase.SupabaseService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DeviceTokenUpdate(
    val token: String,
    val user_id: String? = null
)

class AuraFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        serviceScope.launch {
            try {
                val supabase = SupabaseService.client
                // In a real app, you would get the user_id from the Auth manager.
                // Assuming anonymous or globally tracked device for now.
                supabase.from("devices").upsert(DeviceTokenUpdate(token = token))
            } catch (e: Exception) {
                Log.e("FCM", "Failed to save token to Supabase", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val settingsRepo = NotificationSettingsRepository(applicationContext)
        val category = remoteMessage.data["category"] ?: "Announcements"

        if (!settingsRepo.isCategoryEnabled(category)) {
            return
        }

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Aura Learning"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You have a new message."
        val imageUrl = remoteMessage.notification?.imageUrl?.toString() ?: remoteMessage.data["image_url"]
        val deepLink = remoteMessage.data["deep_link"]
        val id = UUID.randomUUID().toString()

        val notificationEntity = NotificationEntity(
            id = id,
            title = title,
            description = body,
            imageUrl = imageUrl,
            category = category,
            deepLink = deepLink,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        serviceScope.launch {
            val repo = NotificationRepository(applicationContext)
            repo.saveNotificationLocally(notificationEntity)
        }

        sendNotification(title, body, deepLink, category, settingsRepo)
    }

    private fun sendNotification(title: String, messageBody: String, deepLink: String?, category: String, settings: NotificationSettingsRepository) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (deepLink != null) {
                putExtra("deep_link", deepLink)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "aura_channel_id"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // Note: Replace ic_launcher_foreground with a proper notification icon if available
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (settings.soundEnabled) {
            notificationBuilder.setSound(defaultSoundUri)
        }
        
        if (settings.vibrationEnabled) {
            notificationBuilder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Aura Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
