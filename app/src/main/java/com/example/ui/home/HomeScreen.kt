package com.example.ui.home

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.repository.notifications.NotificationRepository
import com.example.ui.ViewModelFactory
import kotlinx.coroutines.launch
import com.example.ui.auth.AuthViewModel
import com.example.data.models.Book
import com.example.data.models.Video
import com.example.data.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel, rootNavController: NavController, viewModel: HomeViewModel = viewModel(factory = ViewModelFactory)) {
    val banners by viewModel.banners.collectAsState()
    val recentBooks by viewModel.recentBooks.collectAsState()
    val recentVideos by viewModel.recentVideos.collectAsState()
    val allBooks by viewModel.allBooks.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val continueReading by viewModel.continueReading.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val selectedGrade = currentUser?.selectedGrade ?: "All Grades"
    
    val context = LocalContext.current
    val notificationRepository = remember { NotificationRepository(context) }
    val unreadCount by notificationRepository.getUnreadCount().collectAsState(initial = 0)

    var showAuthModal by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val interceptAction: (() -> Unit) -> Unit = { action ->
        if (currentUser?.id == "guest_user") {
            pendingAction = action
            showAuthModal = true
        } else {
            action()
        }
    }

    if (showAuthModal) {
        com.example.ui.auth.AuthModal(
            authViewModel = authViewModel,
            onDismissRequest = { 
                showAuthModal = false 
                pendingAction = null
            },
            onAuthSuccess = {
                showAuthModal = false
                pendingAction?.invoke()
                pendingAction = null
            }
        )
    }

    LaunchedEffect(selectedGrade) {
        viewModel.setSelectedGrade(selectedGrade)
    }

    val savedBooks = allBooks.filter { currentUser?.savedBooks?.contains(it.id) == true }
    var searchQuery by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedSubject by viewModel.selectedSubject.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Subjects",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Divider()
                val subjects = listOf("All Subjects", "Mathematics", "Science", "History", "English", "Hindi", "Biology", "Chemistry", "Physics", "Computer Science")
                subjects.forEach { subject ->
                    NavigationDrawerItem(
                        label = { Text(text = subject) },
                        selected = subject == selectedSubject,
                        onClick = {
                            viewModel.setSelectedSubject(subject)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Good Morning, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "Student"} 👋",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedSubject != "All Subjects") "Subject: $selectedSubject" else "Let's continue learning!",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { rootNavController.navigate("notifications") }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge { Text(unreadCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            }
                        }
                        IconButton(onClick = { /* navigate to profile */ }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp) // Add padding for bottom nav if needed
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search books, videos, notes...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { navController.navigate("global_search") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    enabled = false // To make the clickable modifier work properly
                )
            }

            // Section 10: Explore Categories (Moved up for better UX or keep as requested)
            item {
                SectionExploreCategories(selectedGrade) { newGrade ->
                    authViewModel.updateSelectedGrade(newGrade)
                }
            }

            // Daily Study Goal
            item {
                DailyStudyGoalCard()
            }

            item {
                CoursesSection(rootNavController)
            }

            // Section 1: Hero Banner Carousel
            item {
                HeroBannerCarousel(context)
            }

            // Section 6: Quick Actions
            item {
                QuickActionsSection(navController, rootNavController)
            }

            // Continue Watching
            if (continueWatching.isNotEmpty()) {
                item {
                    VideoClassesSection(continueWatching, rootNavController, interceptAction, "Continue Watching")
                }
            }

            // Continue Reading
            if (continueReading.isNotEmpty()) {
                item {
                    ContinueLearningSection(continueReading, rootNavController, interceptAction, "Resume Reading")
                }
            } else if (savedBooks.isNotEmpty()) {
                item {
                    ContinueLearningSection(savedBooks, rootNavController, interceptAction, "Saved Books")
                }
            }

            // Section 3: Popular Books
            if (recentBooks.isNotEmpty()) {
                item {
                    PopularBooksSection(recentBooks, rootNavController, currentUser, authViewModel, interceptAction)
                }
            }

            // Section 8: Daily Motivation Banner
            item {
                DailyMotivationBanner()
            }

            // Section 4: Video Classes
            if (recentVideos.isNotEmpty()) {
                item {
                    VideoClassesSection(recentVideos, rootNavController, interceptAction)
                }
            }

            // Section 5: AI Learning Tools
            item {
                AILearningToolsSection(rootNavController)
            }

            // Section 7: Recommended For You
            item {
                RecommendedSection(recentBooks, recentVideos, rootNavController, interceptAction)
            }
            
            // Section 9: Recently Added Content
            item {
                RecentlyAddedSection(recentBooks, recentVideos, rootNavController, interceptAction)
            }
        }
    }
    }
}

@Composable
fun HeroBannerCarousel(context: android.content.Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                val url = "https://auralearningwebsite.netlify.app"
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, Uri.parse(url))
            },
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🚀 New Updates Available",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Check the latest features, announcements, study updates, and upcoming tools.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val url = "https://auralearningwebsite.netlify.app"
                        val builder = CustomTabsIntent.Builder()
                        val customTabsIntent = builder.build()
                        customTabsIntent.launchUrl(context, Uri.parse(url))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("🌐 Visit Website")
                }
            }
        }
    }
}

@Composable
fun SectionExploreCategories(selectedGrade: String, onGradeSelected: (String) -> Unit) {
    val categories = listOf("All Grades", "Mathematics", "Science", "English", "Hindi", "SST", "Computer", "Biology", "Chemistry", "Physics")
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedGrade == category || (selectedGrade == "All Grades" && category == "All Grades"),
                onClick = { onGradeSelected(category) },
                label = { Text(category) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController, rootNavController: NavController) {
    val actions = listOf(
        Pair("Books", Icons.AutoMirrored.Filled.MenuBook),
        Pair("Videos", Icons.Filled.PlayCircle),
        Pair("Notes", Icons.AutoMirrored.Filled.Notes),
        Pair("PYQs", Icons.Filled.HistoryEdu),
        Pair("Mock Tests", Icons.Filled.Quiz),
        Pair("Study Planner", Icons.Filled.Event),
        Pair("Reminder", Icons.Filled.Alarm),
        Pair("Progress", Icons.Filled.Analytics)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(actions) { (label, icon) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .clickable { 
                        if (label == "Books") navController.navigate("books")
                        if (label == "Videos") navController.navigate("videos")
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ContinueLearningSection(savedBooks: List<Book>, rootNavController: NavController, interceptAction: (() -> Unit) -> Unit, title: String = "Continue Learning") {
    SectionHeader(title)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(savedBooks) { book ->
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .clickable {
                        interceptAction {
                            if (book.pdfUrl.isNotEmpty()) {
                                val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                                rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                        contentDescription = book.bookName,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(book.bookName, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${book.className} • ${book.subject}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 0.4f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PopularBooksSection(recentBooks: List<Book>, rootNavController: NavController, currentUser: User?, authViewModel: AuthViewModel, interceptAction: (() -> Unit) -> Unit) {
    SectionHeader("Popular Books")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recentBooks) { book ->
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .clickable {
                        interceptAction {
                            if (book.pdfUrl.isNotEmpty()) {
                                val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                                rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Box {
                        AsyncImage(
                            model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                            contentDescription = book.bookName,
                            modifier = Modifier
                                .height(180.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { authViewModel.toggleSaveBook(book.id) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .size(32.dp)
                        ) {
                            val isSaved = currentUser?.savedBooks?.contains(book.id) == true
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save Book",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(book.bookName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${book.className} • ${book.subject}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                interceptAction {
                                    if (book.pdfUrl.isNotEmpty()) {
                                        val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                                        rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Read", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoClassesSection(recentVideos: List<Video>, rootNavController: NavController, interceptAction: (() -> Unit) -> Unit, title: String = "Video Classes") {
    SectionHeader(title)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recentVideos) { video ->
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .clickable { 
                        interceptAction {
                            rootNavController.navigate("video_player/${video.id}") 
                        }
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Box {
                        AsyncImage(
                            model = video.thumbnail.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                            contentDescription = video.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = video.duration.ifEmpty { "12:30" }, // Placeholder duration
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(video.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${video.subject} • ${video.teacher.ifEmpty { "Instructor" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AILearningToolsSection(rootNavController: NavController) {
    SectionHeader("AI Learning Tools")
    
    val tools = listOf(
        Pair("AI Tutor", Icons.Filled.SmartToy),
        Pair("Chapter Summary", Icons.Filled.Summarize),
        Pair("Mind Maps", Icons.Filled.AccountTree),
        Pair("Flashcards", Icons.Filled.Style),
        Pair("Notes Generator", Icons.Filled.AutoFixHigh),
        Pair("Translator", Icons.Filled.Translate),
        Pair("PDF Reader", Icons.Filled.PictureAsPdf),
        Pair("MCQ Test", Icons.Filled.Quiz)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tools) { (name, icon) ->
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .clickable { 
                        when (name) {
                            "AI Tutor" -> rootNavController.navigate("ai_chat")
                            "PDF Reader" -> rootNavController.navigate("pdf_tool")
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Icon(icon, contentDescription = name, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("AI Powered", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.align(Alignment.End))
                }
            }
        }
    }
}

@Composable
fun DailyMotivationBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"Success starts with one page today.\"",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text("Start Studying")
                }
            }
        }
    }
}

@Composable
fun RecommendedSection(recentBooks: List<Book>, recentVideos: List<Video>, rootNavController: NavController, interceptAction: (() -> Unit) -> Unit) {
    if (recentBooks.isEmpty()) return
    SectionHeader("Recommended For You")
    // Simplified mix of content
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recentBooks.shuffled().take(3)) { book ->
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .clickable {
                        interceptAction {
                            if (book.pdfUrl.isNotEmpty()) {
                                val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                                rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    AsyncImage(
                        model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                        contentDescription = book.bookName,
                        modifier = Modifier
                            .height(140.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                    Text(book.bookName, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun RecentlyAddedSection(recentBooks: List<Book>, recentVideos: List<Video>, rootNavController: NavController, interceptAction: (() -> Unit) -> Unit) {
    if (recentVideos.isEmpty()) return
    SectionHeader("Recently Added")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recentVideos.take(3)) { video ->
            Card(
                modifier = Modifier
                    .width(200.dp)
                    .clickable { 
                        interceptAction {
                            rootNavController.navigate("video_player/${video.id}")
                        }
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Box {
                        AsyncImage(
                            model = video.thumbnail.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                            contentDescription = video.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentScale = ContentScale.Crop
                        )
                        Badge(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text("NEW", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                    Text(video.title, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}
@Composable
fun CoursesSection(navController: NavController) {
    val repository = com.example.data.repository.AuraRepository()
    val viewModel: com.example.ui.courses.CourseViewModel = viewModel(factory = com.example.ui.ViewModelFactory)
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
            Text(text = "Courses & Subjects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = { navController.navigate("courses") }) { Text("See All") } 
        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses) { course ->
                    CourseCardMini(course = course, onClick = { /* TODO */ })
                }
            }
        }
    }
}

@Composable
fun CourseCardMini(course: com.example.data.models.Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AsyncImage(
                model = course.thumbnailUrl,
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
