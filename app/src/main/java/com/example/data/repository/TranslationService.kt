package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TranslationService {
    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun translateText(text: String, fromLang: String, toLang: String, tone: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API key is missing. Please configure it in the Secrets panel."
        }
        
        val systemPrompt = "You are an expert translator specializing in educational study notes. Translate the following text from $fromLang to $toLang. Ensure the tone is $tone. Preserve any formatting like bullet points, numbering, bold (**text**), italics (*text*), and structure as much as possible. Maintain educational accuracy and context. Output ONLY the translated text, without any additional conversational filler."

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = text))
                )
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.3f
            )
        )
        
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Translation failed."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun improveGrammar(text: String): String = withContext(Dispatchers.IO) {
        val systemPrompt = "You are an expert editor. Fix the grammar, punctuation, and spelling of the following study notes without changing their original meaning or structure. Output ONLY the corrected text."
        processWithPrompt(text, systemPrompt)
    }

    suspend fun simplifyNotes(text: String): String = withContext(Dispatchers.IO) {
        val systemPrompt = "Simplify the following study notes to make them easy for a student to understand. Break down complex concepts into simple terms. Preserve the main points and structure. Output ONLY the simplified text."
        processWithPrompt(text, systemPrompt)
    }

    suspend fun summarizeNotes(text: String): String = withContext(Dispatchers.IO) {
        val systemPrompt = "Provide a concise summary of the following study notes. Extract the key takeaways and main concepts. Output ONLY the summary."
        processWithPrompt(text, systemPrompt)
    }

    private suspend fun processWithPrompt(text: String, systemPrompt: String): String = withContext(Dispatchers.IO) {
         if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API key is missing. Please configure it in the Secrets panel."
        }
        
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = text))
                )
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.2f
            )
        )
        
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Operation failed."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
