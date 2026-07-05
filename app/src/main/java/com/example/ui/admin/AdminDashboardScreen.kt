package com.example.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.models.Book
import com.example.data.models.Video

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Dashboard") }
    val tabs = listOf("Dashboard", "Books", "Videos")

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            when (tab) {
                                "Dashboard" -> Icon(Icons.Filled.Dashboard, contentDescription = null)
                                "Books" -> Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                                "Videos" -> Icon(Icons.Filled.PlayCircle, contentDescription = null)
                            }
                        },
                        label = { Text(tab) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                "Dashboard" -> DashboardContent(viewModel, navController)
                "Books" -> BooksManageContent(viewModel)
                "Videos" -> VideosManageContent(viewModel)
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun DashboardContent(viewModel: AdminViewModel, navController: NavController) {
    val totalUsers by viewModel.totalUsers.collectAsState()
    val books by viewModel.books.collectAsState()
    val videos by viewModel.videos.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Users", totalUsers.toString(), Modifier.weight(1f))
            StatCard("Books", books.size.toString(), Modifier.weight(1f))
            StatCard("Videos", videos.size.toString(), Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { android.widget.Toast.makeText(context, "Manage Categories coming soon", android.widget.Toast.LENGTH_SHORT).show() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Manage Categories")
            }
            Button(
                onClick = { navController.navigate("admin_notifications") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Send Notification")
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun BooksManageContent(viewModel: AdminViewModel) {
    val books by viewModel.books.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Books", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { showAddDialog = true }) {
                Text("Add Book")
            }
        }

        LazyColumn {
            items(books) { book ->
                ListItem(
                    headlineContent = { Text(book.bookName) },
                    supportingContent = { Text(book.className) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { bookToEdit = book }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.deleteBook(book.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddDialog) {
        AddBookDialog(
            initialBook = null,
            onDismiss = { showAddDialog = false },
            onAdd = { book ->
                viewModel.addBook(book)
                showAddDialog = false
            }
        )
    }

    if (bookToEdit != null) {
        AddBookDialog(
            initialBook = bookToEdit,
            onDismiss = { bookToEdit = null },
            onAdd = { book ->
                viewModel.updateBook(book)
                bookToEdit = null
            }
        )
    }
}

@Composable
fun VideosManageContent(viewModel: AdminViewModel) {
    val videos by viewModel.videos.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var videoToEdit by remember { mutableStateOf<Video?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Videos", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { showAddDialog = true }) {
                Text("Add Video")
            }
        }

        LazyColumn {
            items(videos) { video ->
                ListItem(
                    headlineContent = { Text(video.title) },
                    supportingContent = { Text(video.className) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { videoToEdit = video }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.deleteVideo(video.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddDialog) {
        AddVideoDialog(
            initialVideo = null,
            onDismiss = { showAddDialog = false },
            onAdd = { video ->
                viewModel.addVideo(video)
                showAddDialog = false
            }
        )
    }

    if (videoToEdit != null) {
        AddVideoDialog(
            initialVideo = videoToEdit,
            onDismiss = { videoToEdit = null },
            onAdd = { video ->
                viewModel.updateVideo(video)
                videoToEdit = null
            }
        )
    }
}

@Composable
fun AddBookDialog(initialBook: Book?, onDismiss: () -> Unit, onAdd: (Book) -> Unit) {
    var title by remember { mutableStateOf(initialBook?.bookName ?: "") }
    var className by remember { mutableStateOf(initialBook?.className ?: "") }
    var coverUrl by remember { mutableStateOf(initialBook?.coverImage ?: "") }
    var pdfUrl by remember { mutableStateOf(initialBook?.pdfUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBook == null) "Add Book" else "Edit Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class Name (e.g. 10th)") })
                OutlinedTextField(value = coverUrl, onValueChange = { coverUrl = it }, label = { Text("Cover Image URL") })
                OutlinedTextField(value = pdfUrl, onValueChange = { pdfUrl = it }, label = { Text("PDF URL") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(Book(id = initialBook?.id ?: "", bookName = title, className = className, coverImage = coverUrl, pdfUrl = pdfUrl))
            }) {
                Text(if (initialBook == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddVideoDialog(initialVideo: Video?, onDismiss: () -> Unit, onAdd: (Video) -> Unit) {
    var title by remember { mutableStateOf(initialVideo?.title ?: "") }
    var className by remember { mutableStateOf(initialVideo?.className ?: "") }
    var subject by remember { mutableStateOf(initialVideo?.subject ?: "") }
    var chapter by remember { mutableStateOf(initialVideo?.chapter ?: "") }
    var partNumber by remember { mutableStateOf(initialVideo?.partNumber?.toString() ?: "1") }
    var teacher by remember { mutableStateOf(initialVideo?.teacher ?: "") }
    var duration by remember { mutableStateOf(initialVideo?.duration ?: "") }
    var thumbnailUrl by remember { mutableStateOf(initialVideo?.thumbnail ?: "") }
    var videoUrl by remember { mutableStateOf(initialVideo?.videoUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialVideo == null) "Add Video" else "Edit Video") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class Name (e.g. 10th)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = chapter, onValueChange = { chapter = it }, label = { Text("Chapter") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = partNumber, onValueChange = { partNumber = it }, label = { Text("Part Number") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Teacher Name") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = thumbnailUrl, onValueChange = { thumbnailUrl = it }, label = { Text("Thumbnail URL") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = videoUrl, onValueChange = { videoUrl = it }, label = { Text("YouTube Video ID / URL") }, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val yId = if (videoUrl.contains("v=")) videoUrl.substringAfter("v=").substringBefore("&")
                          else if (videoUrl.contains("youtu.be/")) videoUrl.substringAfter("youtu.be/").substringBefore("?")
                          else if (videoUrl.contains("live/")) videoUrl.substringAfter("live/").substringBefore("?")
                          else videoUrl
                onAdd(Video(
                    id = initialVideo?.id ?: "", 
                    title = title, 
                    className = className, 
                    subject = subject,
                    chapter = chapter,
                    partNumber = partNumber.toIntOrNull() ?: 1,
                    teacher = teacher,
                    duration = duration,
                    thumbnail = thumbnailUrl, 
                    videoUrl = videoUrl,
                    youtubeVideoId = yId
                ))
            }) {
                Text(if (initialVideo == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
