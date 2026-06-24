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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(navController: NavController, viewModel: VideosViewModel = viewModel(factory = ViewModelFactory)) {
    val videos by viewModel.videos.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    val classes = listOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Videos") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = if (selectedClass == null) 0 else classes.indexOf(selectedClass) + 1,
                edgePadding = 16.dp
            ) {
                Tab(selected = selectedClass == null, onClick = { viewModel.fetchVideos(null) }, text = { Text("All") })
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
                items(videos) { video ->
                    val context = LocalContext.current
                    Card(modifier = Modifier.fillMaxWidth().height(250.dp).clickable { 
                        if (video.videoUrl.isNotEmpty()) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(video.videoUrl))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "No app found to open link", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Column {
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = video.title,
                                modifier = Modifier.height(180.dp).fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                            Text(video.title, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(video.className, modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

