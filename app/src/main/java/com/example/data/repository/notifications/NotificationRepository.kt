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
            val remoteIds = remoteNotifications.map { it.id }.toSet()
            
            val localNotifications = notificationDao.getNotificationsList()
            val localMap = localNotifications.associateBy { it.id }

            // Delete local ones that are no longer on Supabase
            localNotifications.forEach { local ->
                if (!remoteIds.contains(local.id)) {
                    notificationDao.deleteNotification(local.id)
                }
            }

            // Insert or update remote ones
            remoteNotifications.forEach { remote ->
                val local = localMap[remote.id]
                val entity = NotificationEntity(
                    id = remote.id,
                    title = remote.title,
                    description = remote.description,
                    imageUrl = remote.image_url,
                    category = remote.category,
                    deepLink = remote.deep_link,
                    timestamp = local?.timestamp ?: System.currentTimeMillis(),
                    isRead = local?.isRead ?: false
                )
                notificationDao.insertNotification(entity)
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
