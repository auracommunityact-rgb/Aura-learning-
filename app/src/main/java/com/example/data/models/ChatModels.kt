package com.example.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Conversation(
    val id: String = "",
    @SerialName("isGroup") val isGroup: Boolean = false,
    @SerialName("groupName") val groupName: String? = null,
    @SerialName("groupPhotoUrl") val groupPhotoUrl: String? = null,
    @SerialName("groupDescription") val groupDescription: String? = null,
    @SerialName("adminId") val adminId: String? = null,
    @SerialName("lastMessageText") val lastMessageText: String? = null,
    @SerialName("lastMessageTime") val lastMessageTime: Long = System.currentTimeMillis(),
    @SerialName("createdAt") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class ConversationMember(
    @SerialName("conversationId") val conversationId: String,
    @SerialName("userId") val userId: String,
    @SerialName("joinedAt") val joinedAt: Long = System.currentTimeMillis(),
    val role: String = "member",
    @SerialName("isMuted") val isMuted: Boolean = false,
    @SerialName("isArchived") val isArchived: Boolean = false,
    @SerialName("isPinned") val isPinned: Boolean = false
)

@Serializable
data class Message(
    val id: String = "",
    @SerialName("conversationId") val conversationId: String,
    @SerialName("senderId") val senderId: String,
    val text: String? = null,
    val type: String = "text",
    @SerialName("attachmentUrl") val attachmentUrl: String? = null,
    @SerialName("replyToId") val replyToId: String? = null,
    @SerialName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updatedAt") val updatedAt: Long? = null,
    @SerialName("isDeleted") val isDeleted: Boolean = false
)

@Serializable
data class MessageRead(
    @SerialName("messageId") val messageId: String,
    @SerialName("userId") val userId: String,
    @SerialName("readAt") val readAt: Long = System.currentTimeMillis()
)

@Serializable
data class MessageReaction(
    @SerialName("messageId") val messageId: String,
    @SerialName("userId") val userId: String,
    val reaction: String,
    @SerialName("createdAt") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class UserPresence(
    @SerialName("userId") val userId: String,
    @SerialName("isOnline") val isOnline: Boolean = false,
    @SerialName("lastSeen") val lastSeen: Long = System.currentTimeMillis()
)
