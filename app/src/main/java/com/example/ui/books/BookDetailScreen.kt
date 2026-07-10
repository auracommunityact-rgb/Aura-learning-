package com.example.ui.books

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.models.Book
import com.example.data.repository.AuraRepository
import com.example.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: String,
    authViewModel: AuthViewModel,
    offlineBooksViewModel: OfflineBooksViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AuraRepository() }

    var book by remember { mutableStateOf<Book?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val offlineBooks by offlineBooksViewModel.offlineBooks.collectAsState()
    val downloadProgress by offlineBooksViewModel.downloadProgress.collectAsState()

    // Fetch book details
    LaunchedEffect(bookId) {
        isLoading = true
        book = repository.getBook(bookId)
        isLoading = false
    }

    val isSaved = currentUser?.savedBooks?.contains(bookId) == true
    val isDownloaded = offlineBooks.any { it.id == bookId }
    val currentProgress = downloadProgress[bookId]

    val navyPrimary = Color(0xFF005AC1)
    val navyDark = Color(0xFF002244)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(navyPrimary, navyDark)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (book != null) {
                        IconButton(onClick = {
                            authViewModel.toggleSaveBook(bookId)
                            val message = if (isSaved) "Removed from Library" else "Saved to Library"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (isSaved) "Remove from Library" else "Save to Library",
                                tint = if (isSaved) navyPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = navyPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading book details...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (book == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Book details not found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Please check your internet connection or verify if this book is still available.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            val currentBook = book!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cover Image Hero Section with beautiful Gradient Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradientBrush)
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .height(260.dp)
                            .shadow(24.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        AsyncImage(
                            model = currentBook.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                            contentDescription = currentBook.bookName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title and Basic Info
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = currentBook.bookName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = navyDark
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(currentBook.subject) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = navyPrimary
                                )
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Class ${currentBook.className}") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = navyPrimary
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Description / Syllabus Detail Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "About this Book",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = navyDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "This official textbook is curated for Class ${currentBook.className} students, focusing comprehensively on ${currentBook.subject}. This resource contains syllabus-aligned chapters, in-depth summaries, diagrams, and standard exercises to support academic excellence and homework preparation.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Format: High-quality PDF",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Text(
                                    "Publisher: Aura EdTech",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Offline Download Action Area
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDownloaded) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Filled.CloudDone else Icons.Filled.CloudDownload,
                                    contentDescription = null,
                                    tint = if (isDownloaded) Color(0xFF2E7D32) else navyPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isDownloaded) "Downloaded Offline" else "Available Offline",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDownloaded) Color(0xFF1B5E20) else navyDark
                                    )
                                    Text(
                                        text = if (isDownloaded) "Read anytime without internet!" else "Download to read offline",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDownloaded) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (currentProgress != null) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                                    CircularProgressIndicator(
                                        progress = { currentProgress / 100f },
                                        color = navyPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        "${currentProgress}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (isDownloaded) {
                                TextButton(
                                    onClick = {
                                        offlineBooksViewModel.deleteOfflineBook(currentBook.id)
                                        Toast.makeText(context, "Removed offline download", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Remove", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        offlineBooksViewModel.downloadBook(currentBook)
                                        Toast.makeText(context, "Starting offline download...", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = navyPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Download", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Gemini AI Doubt Solver Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val encodedPrompt = URLEncoder.encode(
                                    "Hi Gemini AI! I am reading the textbook '${currentBook.bookName}' for ${currentBook.subject} (Class ${currentBook.className}). Can you help me study this book and solve standard questions from it?",
                                    "UTF-8"
                                )
                                navController.navigate("ai_chat?prompt=$encodedPrompt")
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = navyPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Ask Gemini AI Tutor 🤖",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = navyPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Stuck on a chapter or exercise? Discuss this textbook directly with Gemini AI for explanations!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    lineHeight = 18.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Discuss with AI",
                                tint = navyPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // Floating Bottom Action Bar for Reading PDF
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = {
                        val localFile = offlineBooks.find { it.id == currentBook.id }
                        val urlToOpen = if (localFile != null) "file://" + localFile.localPdfPath else currentBook.pdfUrl
                        if (urlToOpen.isNotEmpty()) {
                            val encodedUrl = URLEncoder.encode(urlToOpen, "UTF-8")
                            navController.navigate("pdf_viewer?url=$encodedUrl")
                        } else {
                            Toast.makeText(context, "Syllabus book PDF path is not ready yet.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = navyPrimary),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isDownloaded) "Open Offline PDF Book" else "Open & Read PDF Book",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
