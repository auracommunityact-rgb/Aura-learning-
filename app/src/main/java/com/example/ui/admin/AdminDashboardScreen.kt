package com.example.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, authViewModel: AuthViewModel) {
    if (!authViewModel.isAdmin) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
            
            AdminActionCard("Manage Books", "Upload, Edit, Delete Books", Icons.Filled.Book) {
                navController.navigate("admin_upload/book")
            }
            AdminActionCard("Manage Videos", "Upload, Edit, Delete Videos", Icons.Filled.VideoLibrary) {
                navController.navigate("admin_upload/video")
            }
            AdminActionCard("Manage Courses", "Upload and Manage Courses", Icons.Filled.VideoLibrary) {
                navController.navigate("admin_upload_course")
            }
            AdminActionCard("Manage Notifications", "Send and Schedule Notifications", Icons.Filled.Notifications) {
                navController.navigate("admin_notifications")
            }
            AdminActionCard("Manage Exam Results Websites", "Add or edit exam result board websites", Icons.Filled.Language) {
                navController.navigate("admin_manage_exams")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
        }
    }
}
