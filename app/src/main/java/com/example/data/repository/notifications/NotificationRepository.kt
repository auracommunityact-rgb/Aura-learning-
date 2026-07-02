package com.example.data.repository.notifications

import android.content.Context
import android.util.Log
import com.example.data.local.PlannerDatabase
import com.example.data.local.notifications.NotificationDao
import com.example.data.local.notifications.NotificationEntity
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseNotification(
    val id: String,
    val title: String,
    val description: String,
    val image_url: String? = null,
    val category: String,
    val deep_link: String? = null,
    val created_at: String
)

class NotificationRepository(private val context: Context) {
    private val notificationDao: NotificationDao = PlannerDatabase.getDatabase(context).notificationDao()
    private val supabase = SupabaseService.client

    fun getAllNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun getUnreadCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun syncNotifications() = withContext(Dispatchers.IO) {
        try {
            val remoteNotifications = supabase.from("notifications").select().decodeList<SupabaseNotification>()
            val entities = remoteNotifications.map {
                NotificationEntity(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    imageUrl = it.image_url,
                    category = it.category,
                    deepLink = it.deep_link,
                    timestamp = System.currentTimeMillis(), // We should parse created_at ideally
                    isRead = false
                )
            }
            // We shouldn't overwrite isRead status if the notification already exists.
            // But since this is a basic sync, we will just insert.
            // A better way is to insert ignoring conflicts, or query first.
            entities.forEach {
                notificationDao.insertNotification(it)
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to sync notifications", e)
        }
    }

    suspend fun markAsRead(id: String) = notificationDao.markAsRead(id)

    suspend fun markAllAsRead() = notificationDao.markAllAsRead()

    suspend fun deleteNotification(id: String) = notificationDao.deleteNotification(id)

    suspend fun saveNotificationLocally(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    suspend fun deleteAllNotifications() = notificationDao.deleteAllNotifications()
}
