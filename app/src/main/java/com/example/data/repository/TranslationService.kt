package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TranslationService {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val languageIdClient = LanguageIdentification.getClient()

    suspend fun identifyLanguage(text: String): String? = withContext(Dispatchers.IO) {
        try {
            val languageCode = languageIdClient.identifyLanguage(text).await()
            if (languageCode == "und") null else languageCode
        } catch (e: Exception) {
            null
        }
    }

    suspend fun translateText(
        text: String, 
        sourceLangCode: String, 
        targetLangCode: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLangCode)
                .setTargetLanguage(targetLangCode)
                .build()
                
            val translator = Translation.getClient(options)
            
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
            
            val result = translator.translate(text).await()
            translator.close()
            result
        } catch (e: Exception) {
            "Error: ${e.message ?: "Failed to translate."}"
        }
    }
    
    // Kept for the rest of the app to remain unchanged
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
