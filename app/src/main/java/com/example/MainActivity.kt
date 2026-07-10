package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.theme.AuraTheme
import com.example.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize AdMob Mobile Ads SDK optimally on startup
        com.example.utils.AdMobManager.initialize(this)
        
        val themeViewModel = ThemeViewModel(this)
        
        val intentData = intent.data
        var initialDeepLink = intent.getStringExtra("deep_link")
        if (intentData != null && (intentData.host == "auralearningwebsite.netlify.app" || intentData.host == "aura.auralearning.workers.dev")) {
            val path = intentData.path
            initialDeepLink = when {
                path == "/ai_chat" || path?.startsWith("/ai_chat") == true -> {
                    val promptParam = intentData.getQueryParameter("prompt")
                    if (promptParam != null) "ai_chat?prompt=${android.net.Uri.encode(promptParam)}" else "ai_chat"
                }
                path == "/courses" || path?.startsWith("/courses") == true -> "courses"
                path == "/pdf_tool" || path?.startsWith("/pdf_tool") == true -> "pdf_tool"
                path?.startsWith("/book_detail/") == true -> {
                    val bookId = path.substringAfter("/book_detail/")
                    "book_detail/$bookId"
                }
                path?.startsWith("/video_player/") == true -> {
                    val videoId = path.substringAfter("/video_player/")
                    "video_player/$videoId"
                }
                else -> {
                    val tabParam = intentData.getQueryParameter("tab")
                    if (tabParam != null) "main?tab=$tabParam" else "main?tab=home"
                }
            }
        }
        
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            
            AuraTheme(useDarkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuraLearningApp(themeViewModel = themeViewModel, initialDeepLink = initialDeepLink)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
