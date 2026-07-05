package com.example.ui.study.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

data class ChatMessage(val text: String, val isUser: Boolean, val isLoading: Boolean = false)

sealed class MapAction {
    data class OpenMapSearch(val query: String) : MapAction()
    data class OpenMapDirections(val destination: String, val origin: String?) : MapAction()
}

class MapAgentViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi! I'm your Map Agent. I can help you find places, routes, and directions. What are you looking for?", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _mapAction = MutableStateFlow<MapAction?>(null)
    val mapAction: StateFlow<MapAction?> = _mapAction.asStateFlow()

    fun clearMapAction() {
        _mapAction.value = null
    }

    fun sendMessage(text: String) {
        val userMsg = ChatMessage(text, isUser = true)
        val loadingMsg = ChatMessage("", isUser = false, isLoading = true)
        _messages.value = _messages.value + userMsg + loadingMsg

        viewModelScope.launch {
            try {
                val response = generateContent(text)
                
                _messages.value = _messages.value.dropLast(1) // Remove loading
                
                if (response.text != null) {
                    _messages.value = _messages.value + ChatMessage(response.text, isUser = false)
                }
                
                if (response.action != null) {
                    _mapAction.value = response.action
                }
                
            } catch (e: Exception) {
                _messages.value = _messages.value.dropLast(1)
                _messages.value = _messages.value + ChatMessage("Error: ${e.message}", isUser = false)
            }
        }
    }

    private suspend fun generateContent(prompt: String): AgentResponse {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        val historyContents = _messages.value.filter { !it.isLoading }.map { msg ->
            Content(
                role = if (msg.isUser) "user" else "model",
                parts = listOf(Part(text = msg.text))
            )
        }.toMutableList()

        // Replace the last message from user with the prompt, as we just added it to history
        // Wait, the prompt is already in the history as the last user message before the loading message.
        // But history also has the first greeting which is from model.

        val tools = listOf(
            buildJsonObject {
                putJsonArray("functionDeclarations") {
                    addJsonObject {
                        put("name", "openGoogleMaps")
                        put("description", "Open Google Maps to show a place, route, or directions")
                        putJsonObject("parameters") {
                            put("type", "OBJECT")
                            putJsonObject("properties") {
                                putJsonObject("query") {
                                    put("type", "STRING")
                                    put("description", "The place name or destination address to search for.")
                                }
                                putJsonObject("mode") {
                                    put("type", "STRING")
                                    put("description", "The mode of maps. Use 'search' for looking up a place, or 'directions' for routing.")
                                }
                                putJsonObject("origin") {
                                    put("type", "STRING")
                                    put("description", "Optional. The starting location for directions. Only used if mode is 'directions'.")
                                }
                            }
                            putJsonArray("required") { add("query"); add("mode") }
                        }
                    }
                }
            }
        )

        val request = GenerateContentRequest(
            contents = historyContents,
            tools = tools,
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a helpful Map Agent. Your job is to help users find places, routes, or directions. You can provide information about places, and if they ask for directions or want to see a place on a map, use the openGoogleMaps function."))
            )
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val firstCandidate = response.candidates?.firstOrNull()
        val parts = firstCandidate?.content?.parts

        var responseText: String? = null
        var action: MapAction? = null

        parts?.forEach { part ->
            if (part.text != null) {
                responseText = part.text
            }
            if (part.functionCall != null) {
                val args = part.functionCall.args
                val query = args?.get("query")?.jsonPrimitive?.content ?: ""
                val mode = args?.get("mode")?.jsonPrimitive?.content ?: "search"
                val origin = args?.get("origin")?.jsonPrimitive?.content
                
                action = if (mode == "directions") {
                    MapAction.OpenMapDirections(query, origin)
                } else {
                    MapAction.OpenMapSearch(query)
                }
                
                if (responseText == null) {
                    responseText = "Opening Google Maps for '$query'..."
                }
            }
        }
        
        return AgentResponse(responseText, action)
    }
    
    data class AgentResponse(val text: String?, val action: MapAction?)
}
