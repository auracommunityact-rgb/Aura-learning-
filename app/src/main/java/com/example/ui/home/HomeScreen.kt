package com.example.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.ViewModelFactory
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder

import com.example.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel, rootNavController: NavController, viewModel: HomeViewModel = viewModel(factory = ViewModelFactory)) {
    val banners by viewModel.banners.collectAsState()
    val recentBooks by viewModel.recentBooks.collectAsState()
    val recentVideos by viewModel.recentVideos.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val savedBooks = allBooks.filter { currentUser?.savedBooks?.contains(it.id) == true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hi, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "Student"}!", color = MaterialTheme.colorScheme.primary) },
                actions = {
                    IconButton(onClick = { navController.navigate("global_search") }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(onClick = { rootNavController.navigate("ai_chat") }) {
                Icon(Icons.Filled.Star, contentDescription = "AI Chat")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Banners Section
            if (banners.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(banners) { banner ->
                        Card(modifier = Modifier.width(300.dp).height(150.dp)) {
                            AsyncImage(
                                model = banner.imageUrl,
                                contentDescription = banner.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // My Saved Books
            if (savedBooks.isNotEmpty()) {
                Text("My Saved Books", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary)
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(savedBooks) { book ->
                        Card(modifier = Modifier.width(120.dp).height(180.dp).clickable {
                            if (book.pdfUrl.isNotEmpty()) {
                                val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                                rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                            }
                        }) {
                            Column {
                                Box {
                                    AsyncImage(
                                        model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                                        contentDescription = book.bookName,
                                        modifier = Modifier.height(120.dp).fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { authViewModel.toggleSaveBook(book.id) },
                                        modifier = Modifier.align(Alignment.TopEnd).size(32.dp).padding(4.dp)
                                    ) {
                                        val isSaved = true
                                        Icon(
                                            imageVector = Icons.Filled.Bookmark,
                                            contentDescription = "Save Book",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(book.bookName, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Featured Books
            Text("Recent Books", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recentBooks) { book ->
                    Card(modifier = Modifier.width(120.dp).height(180.dp).clickable {
                        if (book.pdfUrl.isNotEmpty()) {
                            val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                            rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                        }
                    }) {
                        Column {
                            Box {
                                AsyncImage(
                                    model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                                    contentDescription = book.bookName,
                                    modifier = Modifier.height(120.dp).fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { authViewModel.toggleSaveBook(book.id) },
                                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp).padding(4.dp)
                                ) {
                                    val isSaved = currentUser?.savedBooks?.contains(book.id) == true
                                    Icon(
                                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Save Book",
                                        tint = if (isSaved) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(book.bookName, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Featured Videos
            Text("Recent Videos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recentVideos) { video ->
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Card(modifier = Modifier.width(160.dp).height(120.dp).clickable {
                        rootNavController.navigate("video_player/${video.id}")
                    }) {
                        Box {
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (video.title.contains("lesson 1", ignoreCase = true)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomEnd = 8.dp),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "Lesson 1",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(video.title, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

