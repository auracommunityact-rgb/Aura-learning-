package com.example.ui.profile.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.BuildConfig
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(navController: NavController, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel?) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsSectionTitle("Appearance")
            SettingsCard {
                val themeMode = themeViewModel?.themeMode?.collectAsState()?.value ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("App Theme", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeOptionButton(
                            icon = Icons.Filled.SettingsBrightness,
                            selected = themeMode == 0,
                            onClick = { themeViewModel?.setThemeMode(0) }
                        )
                        ThemeOptionButton(
                            icon = Icons.Filled.LightMode,
                            selected = themeMode == 1,
                            onClick = { themeViewModel?.setThemeMode(1) }
                        )
                        ThemeOptionButton(
                            icon = Icons.Filled.DarkMode,
                            selected = themeMode == 2,
                            onClick = { themeViewModel?.setThemeMode(2) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionTitle("General")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Filled.CloudUpload,
                    title = "Check Upload",
                    subtitle = "Open latest uploads on website",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://auralearningwebsite.netlify.app/"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handle gracefully
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About App",
                    onClick = { navController.navigate("about_app") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionTitle("Legal")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy Policy",
                    onClick = { navController.navigate("privacy_policy") }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                SettingsItem(
                    icon = Icons.Filled.Description,
                    title = "Terms of Use",
                    onClick = { navController.navigate("terms_of_use") }
                )
            }

            if (authViewModel.isAdmin) {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionTitle("Admin")
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Filled.AdminPanelSettings,
                        title = "Admin Panel",
                        subtitle = "Manage app content",
                        onClick = { navController.navigate("admin_dashboard") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Sign Out", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ThemeOptionButton(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(icon, contentDescription = null)
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About App", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Aura Learning", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Developer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Aura Community Act", style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Aura Learning is a premium educational platform helping students learn better through modern tools and materials.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Text("© 2026 Aura Learning. All rights reserved.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(navController: NavController, title: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "This is a placeholder for $title. Please update this with actual content in a production environment.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
