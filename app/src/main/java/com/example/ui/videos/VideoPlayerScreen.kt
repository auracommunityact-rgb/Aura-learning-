package com.example.ui.videos

import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    videoId: String,
    viewModel: VideoPlayerViewModel = viewModel(factory = ViewModelFactory)
) {
    val video by viewModel.video.collectAsState()
    val chapterVideos by viewModel.chapterVideos.collectAsState()
    val relatedBooks by viewModel.relatedBooks.collectAsState()
    val suggestedVideos by viewModel.suggestedVideos.collectAsState()

    var showAllParts by remember { mutableStateOf(false) }

    LaunchedEffect(videoId) {
        viewModel.loadVideo(videoId)
    }

    if (showAllParts) {
        ModalBottomSheet(onDismissRequest = { showAllParts = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("All Parts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(chapterVideos) { part ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAllParts = false
                                    viewModel.loadVideo(part.id)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = part.thumbnail.ifEmpty { "https://images.unsplash.com/photo-1596496050827-8299e0220de1?auto=format&fit=crop&w=300&q=80" },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp, 60.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Part ${part.partNumber}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(part.title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (part.id == video?.id) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (video == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Video Player
                item {
                    val context = LocalContext.current
                    val lifecycleOwner = LocalLifecycleOwner.current
                    var webView by remember { mutableStateOf<WebView?>(null) }
                    
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> webView?.onResume()
                                Lifecycle.Event.ON_PAUSE -> webView?.onPause()
                                Lifecycle.Event.ON_DESTROY -> webView?.destroy()
                                else -> {}
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                            webView?.destroy()
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    settings.useWideViewPort = true
                                    settings.loadWithOverviewMode = true
                                    settings.allowContentAccess = true
                                    settings.allowFileAccess = true
                                    webChromeClient = WebChromeClient()
                                    webViewClient = WebViewClient()
                                    webView = this
                                }
                            },
                            update = { view ->
                                val embedUrl = "https://www.youtube.com/embed/${video!!.youtubeVideoId}?autoplay=1&playsinline=1&rel=0&modestbranding=1&origin=https://www.youtube.com"
                                val html = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                                        <style>
                                            body { margin: 0; padding: 0; background-color: #000; }
                                            .video-container { position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; }
                                            .video-container iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
                                        </style>
                                    </head>
                                    <body>
                                        <div class="video-container">
                                            <iframe src="$embedUrl" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
                                        </div>
                                    </body>
                                    </html>
                                """.trimIndent()
                                view.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Having trouble playing?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    val intentApp = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${video!!.youtubeVideoId}"))
                                    val intentBrowser = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video!!.youtubeVideoId}"))
                                    try {
                                        context.startActivity(intentApp)
                                    } catch (ex: ActivityNotFoundException) {
                                        try {
                                            context.startActivity(intentBrowser)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open in YouTube App / Browser", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Video Details
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(video!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Badge { Text(video!!.subject) }
                            Badge { Text("Class ${video!!.className}") }
                            if (video!!.duration.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) { Text(video!!.duration) }
                            }
                        }
                        if (video!!.teacher.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("By ${video!!.teacher}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (video!!.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(video!!.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val isWatched by viewModel.isWatched.collectAsState()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.markAsWatched() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWatched) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    if (isWatched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isWatched) "Watched" else "Mark as Watched")
                            }
                            
                            Button(
                                onClick = { navController.navigate("quiz/${video!!.id}") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Take Quiz")
                            }
                        }
                    }
                }

                // Chapter Parts
                if (chapterVideos.size > 1) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Parts of this Chapter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { showAllParts = true }) {
                                    Text("All Parts")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            chapterVideos.forEach { part ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clickable { viewModel.loadVideo(part.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (part.id == video!!.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Part ${part.partNumber}", modifier = Modifier.padding(end = 16.dp, start = 8.dp), fontWeight = FontWeight.Bold)
                                        Text(part.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        if (part.id == video!!.id) {
                                            Text("Playing", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Related Books
                if (relatedBooks.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                "Related Books",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(relatedBooks) { book ->
                                    Card(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .clickable { navController.navigate("pdf_viewer?url=${book.pdfUrl}") }
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=300&q=80" },
                                                contentDescription = book.bookName,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(book.bookName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                                Text(book.subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Suggested Videos
                if (suggestedVideos.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                "Continue Learning",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(suggestedVideos) { v ->
                                    Card(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .clickable { viewModel.loadVideo(v.id) }
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = v.thumbnail.ifEmpty { "https://images.unsplash.com/photo-1596496050827-8299e0220de1?auto=format&fit=crop&w=300&q=80" },
                                                contentDescription = v.title,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(112.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(v.title, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(v.className + " • " + v.subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
