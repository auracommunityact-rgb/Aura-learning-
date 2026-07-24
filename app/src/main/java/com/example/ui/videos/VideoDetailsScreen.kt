package com.example.ui.videos

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.local.PlannerDatabase
import com.example.ui.ViewModelFactory
import com.example.ui.home.HomeViewModel
import kotlinx.coroutines.launch

fun extractYouTubeVideoId(url: String, fallbackId: String): String? {
    if (fallbackId.isNotBlank() && fallbackId.length >= 10) return fallbackId
    if (url.isBlank()) return null

    val patterns = listOf(
        "youtu\\.be/([a-zA-Z0-9_-]{11})".toRegex(),
        "youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})".toRegex(),
        "youtube\\.com/embed/([a-zA-Z0-9_-]{11})".toRegex(),
        "youtube\\.com/shorts/([a-zA-Z0-9_-]{11})".toRegex(),
        "v=([a-zA-Z0-9_-]{11})".toRegex()
    )

    for (regex in patterns) {
        val match = regex.find(url)
        if (match != null && match.groupValues.size > 1) {
            return match.groupValues[1]
        }
    }

    if (url.matches("^[a-zA-Z0-9_-]{11}$".toRegex())) {
        return url
    }

    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailsScreen(
    videoId: String,
    navController: NavController,
    authViewModel: com.example.ui.auth.AuthViewModel = viewModel(factory = ViewModelFactory),
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allVideos by viewModel.allVideos.collectAsState()
    val video = allVideos.find { it.id == videoId }

    var isShortcutPinned by remember(videoId) {
        mutableStateOf(
            com.example.utils.ShortcutHelper.isShortcutPinned(
                context,
                "video_$videoId"
            )
        )
    }

    val currentUser by authViewModel.currentUser.collectAsState()
    val isSaved = currentUser?.savedVideos?.contains(videoId) == true

    var showAllParts by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val noteDao = PlannerDatabase.getDatabase(context).noteDao()
    val noteViewModel: com.example.ui.notes.NoteTakingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.example.ui.notes.NoteTakingViewModel(noteDao) as T
            }
        }
    )

    val activity = context as? com.example.MainActivity
    val isInPip by (activity?.isInPipMode ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsState(initial = false)

    if (isInPip && video != null) {
        val resolvedId = extractYouTubeVideoId(video.videoUrl, video.youtubeVideoId)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val lifecycleOwner = LocalLifecycleOwner.current
            var webView by remember { mutableStateOf<WebView?>(null) }
            var hasPlaybackError by remember(videoId) { mutableStateOf(false) }

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

            if (!resolvedId.isNullOrBlank() && !hasPlaybackError) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        hasPlaybackError = true
                                    }
                                }
                            }
                            webView = this
                        }
                    },
                    update = { view ->
                        val embedUrl = "https://www.youtube.com/embed/$resolvedId?autoplay=1&playsinline=1&rel=0&modestbranding=1&enablejsapi=1"
                        val html = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                                <style>
                                    body { margin: 0; padding: 0; background-color: #000; overflow: hidden; }
                                    .video-container { position: relative; width: 100vw; height: 100vh; }
                                    .video-container iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
                                </style>
                            </head>
                            <body>
                                <div class="video-container">
                                    <iframe src="$embedUrl" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
                                </div>
                            </body>
                            </html>
                        """.trimIndent()
                        view.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        return
    }

    if (video == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val resolvedVideoId = extractYouTubeVideoId(video.videoUrl, video.youtubeVideoId)
    val isInvalidUrl = resolvedVideoId.isNullOrBlank()

    if (showAllParts) {
        ModalBottomSheet(onDismissRequest = { showAllParts = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "All Chapter Parts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                val chapterVideos = allVideos.filter {
                    it.chapter.equals(video.chapter, ignoreCase = true) &&
                            it.subject.equals(video.subject, ignoreCase = true) &&
                            it.className.equals(video.className, ignoreCase = true)
                }.sortedBy { it.partNumber }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(chapterVideos) { part ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAllParts = false
                                    navController.navigate("video_details/${part.id}") {
                                        popUpTo("video_details/$videoId") { inclusive = true }
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = part.thumbnail.ifEmpty { video.thumbnail },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp, 60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Part ${part.partNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    part.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (part.id == videoId) FontWeight.Bold else FontWeight.Normal
                                )
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
                title = {
                    Text(
                        video.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        com.example.utils.ShareHelper.shareContent(
                            context = context,
                            title = video.title,
                            contentType = "video",
                            id = video.id
                        )
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = {
                        coroutineScope.launch {
                            if (isShortcutPinned) {
                                com.example.utils.ShortcutHelper.removeShortcut(
                                    context,
                                    "video_$videoId",
                                    video.title
                                )
                                isShortcutPinned = false
                            } else {
                                com.example.utils.ShortcutHelper.pinShortcut(
                                    context = context,
                                    id = "video_$videoId",
                                    title = video.title,
                                    imageUrl = video.thumbnail,
                                    type = "video",
                                    internalRoute = "video_details/$videoId"
                                )
                                isShortcutPinned = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Add to Home Screen",
                            tint = if (isShortcutPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            com.example.ui.notes.FloatingNoteButton(onNoteClick = { showNoteDialog = true })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1. Video Player At Top
            item {
                val lifecycleOwner = LocalLifecycleOwner.current
                var webView by remember { mutableStateOf<WebView?>(null) }
                var hasPlaybackError by remember(videoId) { mutableStateOf(false) }

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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .background(Color.Black)
                ) {
                    if (isInvalidUrl) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "This video link is invalid.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open in YouTube")
                            }
                        }
                    } else if (hasPlaybackError) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "This video cannot be played inside the app because embedding is disabled by the owner.",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val intentApp = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("vnd.youtube:$resolvedVideoId")
                                    )
                                    val intentBrowser = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.youtube.com/watch?v=$resolvedVideoId")
                                    )
                                    try {
                                        context.startActivity(intentApp)
                                    } catch (ex: ActivityNotFoundException) {
                                        try {
                                            context.startActivity(intentBrowser)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open in YouTube")
                            }
                        }
                    } else {
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
                                    webViewClient = object : WebViewClient() {
                                        override fun onReceivedError(
                                            view: WebView?,
                                            request: WebResourceRequest?,
                                            error: WebResourceError?
                                        ) {
                                            super.onReceivedError(view, request, error)
                                            if (request?.isForMainFrame == true) {
                                                hasPlaybackError = true
                                            }
                                        }
                                    }
                                    webView = this
                                }
                            },
                            update = { view ->
                                val embedUrl =
                                    "https://www.youtube.com/embed/$resolvedVideoId?autoplay=1&playsinline=1&rel=0&modestbranding=1&enablejsapi=1"
                                val html = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                                        <style>
                                            body { margin: 0; padding: 0; background-color: #000; overflow: hidden; }
                                            .video-container { position: relative; width: 100vw; height: 100vh; }
                                            .video-container iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
                                        </style>
                                    </head>
                                    <body>
                                        <div class="video-container">
                                            <iframe src="$embedUrl" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
                                        </div>
                                    </body>
                                    </html>
                                """.trimIndent()
                                view.loadDataWithBaseURL(
                                    "https://www.youtube.com",
                                    html,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 2. Video Title & Metadata Below
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1.2M views",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "2 days ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (video.duration.isNotBlank()) {
                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = video.duration,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        VideoActionButton(Icons.Default.ThumbUp, "12K") {}
                        VideoActionButton(Icons.Default.ThumbDown, "Dislike") {}
                        VideoActionButton(Icons.Default.Share, "Share") {
                            com.example.utils.ShareHelper.shareContent(
                                context = context,
                                title = video.title,
                                contentType = "video",
                                id = video.id
                            )
                        }
                        VideoActionButton(
                            if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            if (isSaved) "Saved" else "Save"
                        ) {
                            authViewModel.toggleSaveVideo(videoId)
                            val msg = if (isSaved) "Removed from Saved" else "Saved to Learning"
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                        VideoActionButton(Icons.Default.PlayArrow, "YouTube") {
                            val intentApp = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("vnd.youtube:$resolvedVideoId")
                            )
                            val intentBrowser = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=$resolvedVideoId")
                            )
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
                        VideoActionButton(Icons.Default.PictureInPicture, "PiP") {
                            val act = context as? android.app.Activity
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                try {
                                    val params = android.app.PictureInPictureParams.Builder().build()
                                    act?.enterPictureInPictureMode(params)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                android.widget.Toast.makeText(context, "PiP not supported on this Android version", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Teacher / Creator Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (video.teacher.isNotBlank()) video.teacher.take(1)
                                    .uppercase() else "T",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.teacher.ifEmpty { "Aura Expert Teacher" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Class ${video.className} • ${video.subject}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                android.widget.Toast.makeText(
                                    context,
                                    "Subscribed to ${video.teacher}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Subscribe")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tags
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text("Class ${video.className}") })
                        SuggestionChip(onClick = {}, label = { Text(video.subject) })
                        if (video.chapter.isNotBlank()) {
                            SuggestionChip(onClick = {}, label = { Text(video.chapter) })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    if (video.description.isNotBlank()) {
                        Text(
                            text = video.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action Buttons (Mark as Watched & Quiz)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                android.widget.Toast.makeText(
                                    context,
                                    "Marked as Watched",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Watched")
                        }

                        Button(
                            onClick = { navController.navigate("quiz/$videoId") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Quiz")
                        }
                    }
                }
            }

            // 3. Chapter Parts if multiple
            val chapterVideos = allVideos.filter {
                it.chapter.equals(video.chapter, ignoreCase = true) &&
                        it.subject.equals(video.subject, ignoreCase = true) &&
                        it.className.equals(video.className, ignoreCase = true)
            }.sortedBy { it.partNumber }

            if (chapterVideos.size > 1) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Parts of this Chapter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { showAllParts = true }) {
                                Text("View All (${chapterVideos.size})")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        chapterVideos.take(3).forEach { part ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable {
                                        navController.navigate("video_details/${part.id}") {
                                            popUpTo("video_details/$videoId") { inclusive = true }
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (part.id == videoId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Part ${part.partNumber}",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        part.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (part.id == videoId) {
                                        Text(
                                            "Playing",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Suggested Videos / Up Next
            item {
                Text(
                    text = "Up Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            val recommendations = allVideos.filter { it.id != videoId && it.className == video.className }.take(6)
            items(recommendations) { rec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("video_details/${rec.id}") {
                                popUpTo("video_details/$videoId") { inclusive = true }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = rec.thumbnail.ifEmpty { "https://images.unsplash.com/photo-1596496050827-8299e0220de1?auto=format&fit=crop&w=300&q=80" },
                        contentDescription = null,
                        modifier = Modifier
                            .width(140.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rec.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${rec.teacher} • Class ${rec.className}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showNoteDialog) {
            com.example.ui.notes.NoteDialog(
                onDismiss = { showNoteDialog = false },
                onSave = { content -> noteViewModel.saveNote(content, "video/$videoId") }
            )
        }
    }
}

@Composable
fun VideoActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
