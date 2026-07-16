package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.repository.AuraRepository
import com.example.utils.ShareHelper
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepLinkLoaderScreen(
    navController: NavController,
    type: String,
    slug: String
) {
    val repository = remember { AuraRepository() }
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun toSlug(str: String): String {
        return str.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .trim { it == '-' }
    }

    fun isMatch(title: String, id: String, query: String): Boolean {
        val q = query.lowercase().trim()
        if (id.lowercase() == q) return true
        val nameSlug = toSlug(title)
        if (nameSlug == q) return true
        
        val queryNormalized = q
            .replace("math-", "mathematics-")
            .replace("sst-", "social-studies-")
            .replace("computer-", "computer-science-")
            
        val nameSlugNormalized = nameSlug
            .replace("mathematics-", "math-")
            .replace("social-studies-", "sst-")
            .replace("computer-science-", "computer-")
            
        if (nameSlug == queryNormalized || nameSlugNormalized == q) return true
        
        return false
    }

    LaunchedEffect(type, slug) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                when (type.lowercase()) {
                    "book" -> {
                        val books = repository.getBooks()
                        val matchedBook = books.find { isMatch(it.bookName, it.id, slug) }
                        if (matchedBook != null) {
                            navController.navigate("book_detail/${matchedBook.id}") {
                                popUpTo("deeplink_loader?type={type}&slug={slug}") { inclusive = true }
                            }
                        } else {
                            errorMessage = "We couldn't find the requested book. It may have been removed or renamed."
                        }
                    }
                    "video" -> {
                        val videos = repository.getVideos()
                        val matchedVideo = videos.find { isMatch(it.title, it.id, slug) }
                        if (matchedVideo != null) {
                            navController.navigate("video_player/${matchedVideo.id}") {
                                popUpTo("deeplink_loader?type={type}&slug={slug}") { inclusive = true }
                            }
                        } else {
                            errorMessage = "We couldn't find the requested video. It may have been removed or renamed."
                        }
                    }
                    "course" -> {
                        val courses = repository.getCourses()
                        val matchedCourse = courses.find { isMatch(it.title, it.id, slug) }
                        if (matchedCourse != null) {
                            val encUrl = URLEncoder.encode(matchedCourse.youtubeUrl, "UTF-8")
                            val encTitle = URLEncoder.encode(matchedCourse.title, "UTF-8")
                            navController.navigate("exam_webview?url=$encUrl&title=$encTitle") {
                                popUpTo("deeplink_loader?type={type}&slug={slug}") { inclusive = true }
                            }
                        } else {
                            errorMessage = "We couldn't find the requested course. It may have been removed or renamed."
                        }
                    }
                    "page" -> {
                        val websites = repository.getWebsites()
                        val matchedPage = websites.find { isMatch(it.name, it.id, slug) }
                        if (matchedPage != null) {
                            val encUrl = URLEncoder.encode(matchedPage.url, "UTF-8")
                            val encTitle = URLEncoder.encode(matchedPage.name, "UTF-8")
                            navController.navigate("exam_webview?url=$encUrl&title=$encTitle") {
                                popUpTo("deeplink_loader?type={type}&slug={slug}") { inclusive = true }
                            }
                        } else {
                            errorMessage = "We couldn't find the requested website or portal page."
                        }
                    }
                    else -> {
                        errorMessage = "Invalid content type specified in deep link."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "An error occurred while connecting to our servers. Please check your internet connection."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aura Link") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Loading aura content...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Content Not Found",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Content Not Found",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "The shared link points to content that is currently unavailable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            navController.navigate("main?tab=home") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back to Home Catalog")
                    }
                }
            }
        }
    }
}
