package com.example.ui.admin.notifications

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class NotificationPayload(
    val id: String,
    val title: String,
    val description: String,
    val image_url: String?,
    val category: String,
    val deep_link: String?,
    val status: String = "sent"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationManagerScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Announcements") }
    var deepLink by remember { mutableStateOf("") }
    
    val categories = listOf("New Books", "New Videos", "New Tools", "App Updates", "Announcements")
    var expanded by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Notification") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = deepLink,
                onValueChange = { deepLink = it },
                label = { Text("Deep Link (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Title and Message are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isSending = true
                    coroutineScope.launch {
                        try {
                            val payload = NotificationPayload(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                image_url = imageUrl.takeIf { it.isNotBlank() },
                                category = category,
                                deep_link = deepLink.takeIf { it.isNotBlank() }
                            )
                            
                            // Insert into Supabase table which can trigger a webhook to actually send FCM or Edge Function
                            SupabaseService.client.from("notifications").insert(payload)
                            
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Notification sent successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } finally {
                            isSending = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send to All Users")
                }
            }
        }
    }
}
