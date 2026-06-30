package com.example.ui.videos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.BookmarkBorder
import com.example.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(navController: NavController, authViewModel: AuthViewModel, rootNavController: NavController, viewModel: VideosViewModel = viewModel(factory = ViewModelFactory)) {
    val videos by viewModel.videos.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
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
    val subjects = listOf("Mathematics", "Science", "English", "Hindi", "Social Studies", "Computer Science")
    
    val videosBySubject = videos.groupBy { it.subject.ifEmpty { "Other" } }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Filters",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
                
                LazyColumn(modifier = Modifier.fillMaxHeight().padding(bottom = 16.dp)) {
                    item {
                        Text(
                            "Grade Level",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text("All Grades") },
                            selected = selectedClass == null,
                            onClick = { 
                                viewModel.setFilters(null, selectedSubject)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    lazyItems(classes) { cls ->
                        NavigationDrawerItem(
                            label = { Text(cls) },
                            selected = selectedClass == cls,
                            onClick = { 
                                viewModel.setFilters(cls, selectedSubject)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "Subject",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text("All Subjects") },
                            selected = selectedSubject == null,
                            onClick = { 
                                viewModel.setFilters(selectedClass, null)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    
                    lazyItems(subjects) { subject ->
                        NavigationDrawerItem(
                            label = { Text(subject) },
                            selected = selectedSubject == subject,
                            onClick = { 
                                viewModel.setFilters(selectedClass, subject)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Video Lessons") },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (videos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No videos found for selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
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
                            lazyItems(subjectVideos) { video ->
                                val context = LocalContext.current
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { 
                                        navController.navigate("video_player/${video.id}")
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
                                            if (video.title.contains("lesson 1", ignoreCase = true)) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomEnd = 8.dp),
                                                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart)
                                                ) {
                                                    Text(
                                                        text = "Lesson 1",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
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
    }
}

