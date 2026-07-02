package com.example.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.repository.notifications.NotificationSettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val settings = remember { NotificationSettingsRepository(context) }
    
    var allEnabled by remember { mutableStateOf(settings.allNotificationsEnabled) }
    var booksEnabled by remember { mutableStateOf(settings.newBooksEnabled) }
    var videosEnabled by remember { mutableStateOf(settings.newVideosEnabled) }
    var toolsEnabled by remember { mutableStateOf(settings.newToolsEnabled) }
    var updatesEnabled by remember { mutableStateOf(settings.updatesEnabled) }
    var announcementsEnabled by remember { mutableStateOf(settings.announcementsEnabled) }
    var soundEnabled by remember { mutableStateOf(settings.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(settings.vibrationEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SettingSwitchItem(
                title = "Allow All Notifications",
                description = "Turn on or off all push notifications",
                checked = allEnabled,
                onCheckedChange = {
                    allEnabled = it
                    settings.allNotificationsEnabled = it
                }
            )
            
            HorizontalDivider()
            
            Text(
                "Categories", 
                style = MaterialTheme.typography.titleSmall, 
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            
            SettingSwitchItem("New Books", "Notify when new books are added", booksEnabled, allEnabled) { 
                booksEnabled = it
                settings.newBooksEnabled = it
            }
            SettingSwitchItem("New Videos", "Notify when new videos are uploaded", videosEnabled, allEnabled) { 
                videosEnabled = it
                settings.newVideosEnabled = it
            }
            SettingSwitchItem("New Tools", "Notify about new tools", toolsEnabled, allEnabled) { 
                toolsEnabled = it
                settings.newToolsEnabled = it
            }
            SettingSwitchItem("App Updates", "Notify about application updates", updatesEnabled, allEnabled) { 
                updatesEnabled = it
                settings.updatesEnabled = it
            }
            SettingSwitchItem("Announcements", "Important announcements and news", announcementsEnabled, allEnabled) { 
                announcementsEnabled = it
                settings.announcementsEnabled = it
            }

            HorizontalDivider()
            
            Text(
                "System", 
                style = MaterialTheme.typography.titleSmall, 
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            
            SettingSwitchItem("Sound", "Play sound on notification", soundEnabled, allEnabled) { 
                soundEnabled = it
                settings.soundEnabled = it
            }
            SettingSwitchItem("Vibration", "Vibrate on notification", vibrationEnabled, allEnabled) { 
                vibrationEnabled = it
                settings.vibrationEnabled = it
            }
        }
    }
}

@Composable
fun SettingSwitchItem(title: String, description: String, checked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
