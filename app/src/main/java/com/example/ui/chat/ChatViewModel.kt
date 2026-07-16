package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Conversation
import com.example.data.models.Message
import com.example.data.repository.ChatRepository
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    
    private val _currentMessages = MutableStateFlow<List<Message>>(emptyList())
    val currentMessages: StateFlow<List<Message>> = _currentMessages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val currentUserId: String?
        get() = SupabaseService.client.auth.currentUserOrNull()?.id
        
    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val convos = repository.getConversations()
                _conversations.value = convos
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val msgs = repository.getMessages(conversationId)
                _currentMessages.value = msgs
                
                // Subscribe to real-time updates
                repository.subscribeToMessages(conversationId).collectLatest { newMessage ->
                    _currentMessages.value = _currentMessages.value + newMessage
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendMessage(conversationId: String, text: String, type: String = "text") {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            try {
                val msg = Message(
                    conversationId = conversationId,
                    senderId = userId,
                    text = text,
                    type = type
                )
                repository.sendMessage(msg)
                repository.updateConversationLastMessage(conversationId, text)
                // New message will be caught by real-time subscription
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
