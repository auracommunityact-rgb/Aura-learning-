package com.example.ui.videos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.ViewModelFactory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import com.example.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(navController: NavController, authViewModel: AuthViewModel, rootNavController: NavController, viewModel: VideosViewModel = viewModel(factory = ViewModelFactory)) {
    val videos by viewModel.videos.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showLoginPrompt by remember { mutableStateOf(false) }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { Text("Sign In Required") },
            text = { Text("You need to sign in to save videos.") },
            confirmButton = {
                TextButton(onClick = { 
                    showLoginPrompt = false
                    rootNavController.navigate("login") 
                }) {
                    Text("Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginPrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val classes = listOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th")
    val videosBySubject = videos.groupBy { it.subject.ifEmpty { "Other" } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Video Lessons") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = if (selectedClass == null) 0 else classes.indexOf(selectedClass) + 1,
                edgePadding = 16.dp
            ) {
                Tab(selected = selectedClass == null, onClick = { viewModel.fetchVideos(null) }, text = { Text("All Grades") })
                classes.forEach { cls ->
                    Tab(
                        selected = selectedClass == cls,
                        onClick = { viewModel.fetchVideos(cls) },
                        text = { Text(cls) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                videosBySubject.forEach { (subject, subjectVideos) ->
                    item {
                        Text(
                            text = subject,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = if (subject == videosBySubject.keys.first()) 0.dp else 16.dp, bottom = 4.dp)
                        )
                    }
                    items(subjectVideos) { video ->
                        val context = LocalContext.current
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                if (video.videoUrl.isNotEmpty()) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(video.videoUrl))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "No app found to open link", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                Box {
                                    AsyncImage(
                                        model = video.thumbnail.ifEmpty { "https://images.unsplash.com/photo-1596496050827-8299e0220de1?auto=format&fit=crop&w=300&q=80" },
                                        contentDescription = video.title,
                                        modifier = Modifier.height(200.dp).fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { 
                                            if (currentUser == null) {
                                                showLoginPrompt = true
                                            } else {
                                                authViewModel.toggleSaveVideo(video.id)
                                            }
                                        },
                                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                                    ) {
                                        val isSaved = currentUser?.savedVideos?.contains(video.id) == true
                                        Icon(
                                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Save Video",
                                            tint = if (isSaved) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(video.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(video.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Grade ${video.className}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

