package com.example.ui.home

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val db = remember { com.example.data.local.PlannerDatabase.getDatabase(context) }
    val examList by db.examDateSheetDao().getAllExamsFlow().collectAsState(initial = emptyList())
    var currentTick by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTick = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }

    val nextExam = remember(examList, currentTick) {
        examList.firstOrNull { it.timestamp > currentTick }
    }

    LaunchedEffect(nextExam) {
        viewModel.setActiveExamSubject(nextExam?.subject)
    }

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
                        IconButton(onClick = { 
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }) {
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = { navController.navigate("global_search") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search books, videos, tools, exams...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Category Grid
            item {
                CategoryGrid(navController)
            }

            // Google, AI Mode & Gemini AI Buttons side-by-side
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google Custom Tab Card
                    Card(
                        modifier = Modifier
                            .weight(1.1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        onClick = {
                            val encodedUrl = android.net.Uri.encode("https://www.google.com")
                            rootNavController.navigate("exam_webview?url=$encodedUrl&title=Search")
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleIcon(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Row {
                                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // AI Mode Card (Search with Gemini Sparkles)
                    Card(
                        modifier = Modifier
                            .weight(1.1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                        onClick = {
                            val encodedUrl = android.net.Uri.encode("https://www.google.com/search?udm=50")
                            rootNavController.navigate("exam_webview?url=$encodedUrl&title=AI Mode")
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.size(18.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.Center).size(16.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFEC4899),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(9.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Gemini AI Card (Icon Only with Premium Gradient)
                    Card(
                        modifier = Modifier
                            .size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
                        onClick = {
                            rootNavController.navigate("ai_chat")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF3B82F6), // Gemini Blue
                                            Color(0xFF8B5CF6), // Gemini Purple
                                            Color(0xFFEC4899)  // Gemini Pink
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Exam Countdown Timer (Earliest upcoming exam)
            if (nextExam != null) {
                item {
                    val diff = nextExam.timestamp - currentTick
                    val days = diff / (24 * 60 * 60 * 1000L)
                    val hours = (diff / (60 * 60 * 1000L)) % 24
                    val minutes = (diff / (60 * 1000L)) % 60
                    val seconds = (diff / 1000L) % 60

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { rootNavController.navigate("exam_countdown") },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)), // Premium Navy Blue
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Exam Countdown: ${nextExam.subject}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Grade ${nextExam.grade} • ${nextExam.examDate} (${nextExam.examDay})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HomeCountdownUnit(value = days.toString().padStart(2, '0'), label = "Days")
                                Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                HomeCountdownUnit(value = hours.toString().padStart(2, '0'), label = "Hrs")
                                Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                HomeCountdownUnit(value = minutes.toString().padStart(2, '0'), label = "Mins")
                                Text(":", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                HomeCountdownUnit(value = seconds.toString().padStart(2, '0'), label = "Secs")
                            }
                        }
                    }
                }
            }

            // Section 10: Explore Categories (Moved up for better UX or keep as requested)
            item {
                SectionExploreCategories(selectedSubject) { newSubject ->
                    viewModel.setSelectedSubject(newSubject)
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
                DailyMotivationBanner(onStartStudying = { navController.navigate("study") })
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
fun CategoryGrid(navController: NavController) {
    val categories = listOf(
        Category("Books", Icons.AutoMirrored.Filled.MenuBook, Color(0xFF1E3A8A), "books"),
        Category("Video Lessons", Icons.Filled.PlayCircle, Color(0xFF1E3A8A), "videos"),
        Category("Resources", Icons.Filled.Folder, Color(0xFF1E3A8A), "resources")
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.height(140.dp)
    ) {
        items(categories) { category ->
            Card(
                onClick = { navController.navigate(category.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = category.color,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }
        }
    }
}

data class Category(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

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
fun SectionExploreCategories(selectedSubject: String, onSubjectSelected: (String) -> Unit) {
    val categories = listOf("All Subjects", "Mathematics", "Science", "English", "Hindi", "SST", "Computer", "Biology", "Chemistry", "Physics")
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedSubject.equals(category, ignoreCase = true) || (selectedSubject == "All Subjects" && category == "All Subjects"),
                onClick = { onSubjectSelected(category) },
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
                            rootNavController.navigate("book_detail/${book.id}")
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
                            rootNavController.navigate("book_detail/${book.id}")
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
                                    rootNavController.navigate("book_detail/${book.id}")
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
        Pair("Gemini AI", Icons.Filled.SmartToy),
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
                            "Gemini AI" -> rootNavController.navigate("ai_chat")
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
fun DailyMotivationBanner(onStartStudying: () -> Unit) {
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
                    onClick = onStartStudying,
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
                            rootNavController.navigate("book_detail/${book.id}")
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
                    CourseCardMini(course = course, onClick = { navController.navigate("courses") })
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

@Composable
fun HomeCountdownUnit(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(52.dp)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp
        )
    }
}

@Composable
fun GoogleIcon(modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val strokeWidth = sizePx * 0.18f
        val radius = (sizePx - strokeWidth) / 2f
        val arcSize = sizePx - strokeWidth
        val centerPoint = center
        
        // Red segment (top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
        )
        // Yellow segment (bottom-left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 100f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
        )
        // Green segment (bottom)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
        )
        // Blue segment (right & horizontal line)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 280f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square),
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
        )
        
        // Horizontal bar of "G"
        val barLength = radius * 0.95f
        drawLine(
            color = Color(0xFF4285F4),
            start = androidx.compose.ui.geometry.Offset(centerPoint.x, centerPoint.y),
            end = androidx.compose.ui.geometry.Offset(centerPoint.x + barLength, centerPoint.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}

