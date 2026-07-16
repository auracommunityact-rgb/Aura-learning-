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
        
        // Register all notification categories/channels with the Android system
        com.example.utils.NotificationHelper.registerNotificationChannels(this)
        
        val themeViewModel = ThemeViewModel(this)
        
        val intentData = intent.data
        var initialDeepLink = intent.getStringExtra("deep_link")
        if (intentData != null && (intentData.host == "auralearningwebsite.netlify.app" || intentData.host == "aura.auralearning.workers.dev")) {
            val path = intentData.path
            val bookParam = intentData.getQueryParameter("book")
            initialDeepLink = when {
                bookParam != null -> "deeplink_loader?type=book&slug=${android.net.Uri.encode(bookParam)}"
                path?.startsWith("/course/") == true -> {
                    val slug = path.substringAfter("/course/")
                    "deeplink_loader?type=course&slug=${android.net.Uri.encode(slug)}"
                }
                path?.startsWith("/video/") == true -> {
                    val slug = path.substringAfter("/video/")
                    "deeplink_loader?type=video&slug=${android.net.Uri.encode(slug)}"
                }
                path?.startsWith("/book/") == true -> {
                    val slug = path.substringAfter("/book/")
                    "deeplink_loader?type=book&slug=${android.net.Uri.encode(slug)}"
                }
                path?.startsWith("/page/") == true -> {
                    val slug = path.substringAfter("/page/")
                    "deeplink_loader?type=page&slug=${android.net.Uri.encode(slug)}"
                }
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

    override fun onResume() {
        super.onResume()
        com.example.ui.chat.UserPresenceManager.setOnline()
    }
    
    override fun onPause() {
        super.onPause()
        com.example.ui.chat.UserPresenceManager.setOffline()
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
