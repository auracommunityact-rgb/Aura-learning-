package com.example.data.repository

import com.example.data.models.*
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import io.github.jan.supabase.postgrest.query.Order

class ChatRepository {
    private val client = SupabaseService.client
    private val postgrest = client.postgrest
    private val auth = client.auth
    private val realtime = client.realtime
    
    suspend fun getConversations(): List<Conversation> {
        val currentUserId = auth.currentUserOrNull()?.id ?: return emptyList()
        val members = postgrest["conversation_members"]
            .select { filter { eq("userId", currentUserId) } }
            .decodeList<ConversationMember>()
            
        if (members.isEmpty()) return emptyList()
        
        val conversationIds = members.map { it.conversationId }
        return postgrest["conversations"]
            .select { filter { isIn("id", conversationIds) } }
            .decodeList<Conversation>()
            .sortedByDescending { it.lastMessageTime }
    }
    
    suspend fun getMessages(conversationId: String): List<Message> {
        return postgrest["messages"]
            .select { 
                filter { eq("conversationId", conversationId) }
                order("createdAt", Order.ASCENDING)
            }
            .decodeList<Message>()
    }
    
    suspend fun sendMessage(message: Message): Message {
        return postgrest["messages"]
            .insert(message) { select() }
            .decodeSingle<Message>()
    }
    
    suspend fun updateConversationLastMessage(conversationId: String, text: String) {
        postgrest["conversations"]
            .update({
                set("lastMessageText", text)
                set("lastMessageTime", System.currentTimeMillis())
            }) {
                filter { eq("id", conversationId) }
            }
    }
    
    fun subscribeToMessages(conversationId: String): Flow<Message> = callbackFlow {
        val channel = realtime.channel("messages-$conversationId")
        val changes = channel.postgresChangeFlow<PostgresAction.Insert>("public") {
            table = "messages"
        }
        
        val job = CoroutineScope(Dispatchers.IO).launch {
            changes.collect { action ->
                try {
                    val message = Json.decodeFromJsonElement<Message>(action.record)
                    if (message.conversationId == conversationId) {
                        trySend(message)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            channel.subscribe()
        }
        
        awaitClose {
            job.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                channel.unsubscribe()
            }
        }
    }
}
