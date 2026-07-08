package com.example.ui.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.auth.AuthViewModel

import com.example.ui.ViewModelFactory
import androidx.compose.foundation.lazy.grid.GridItemSpan
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    rootNavController: NavController,
    viewModel: BooksViewModel = viewModel(factory = ViewModelFactory),
    offlineBooksViewModel: OfflineBooksViewModel = viewModel()
) {
    val books by viewModel.books.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser?.selectedGrade) {
        val grade = currentUser?.selectedGrade ?: "All Grades"
        val initialClass = if (grade == "All Grades") null else {
            val gradeStr = grade.replace("Grade ", "")
            val isNumeric = gradeStr.any { it.isDigit() }
            if (isNumeric && gradeStr.isNotEmpty()) {
                val cleanGrade = gradeStr.filter { it.isDigit() }
                when (cleanGrade) {
                    "1" -> "1st"
                    "2" -> "2nd"
                    "3" -> "3rd"
                    else -> "${cleanGrade}th"
                }
            } else {
                null
            }
        }
        viewModel.setFilters(initialClass, selectedSubject)
    }

    val offlineBooks by offlineBooksViewModel.offlineBooks.collectAsState()
    val downloadProgress by offlineBooksViewModel.downloadProgress.collectAsState()

    var showLoginPrompt by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { Text("Sign In Required") },
            text = { Text("You need to sign in to save books.") },
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
    
    val booksBySubject = books.groupBy { it.subject.ifEmpty { "Other" } }
    
    val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
    val newBooks = books.filter { it.createdAt >= sevenDaysAgo }
    
    val offlineBooksList = offlineBooks.map { 
        com.example.data.models.Book(
            id = it.id,
            bookName = it.bookName,
            className = it.className,
            subject = it.subject,
            coverImage = it.coverImage,
            pdfUrl = "file://" + it.localPdfPath,
            createdAt = it.downloadedAt
        )
    }
    val offlineBooksBySubject = offlineBooksList.groupBy { it.subject.ifEmpty { "Other" } }

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
                HorizontalDivider()
                
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
                                authViewModel.updateSelectedGrade("All Grades")
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    items(classes) { cls ->
                        NavigationDrawerItem(
                            label = { Text(cls) },
                            selected = selectedClass == cls,
                            onClick = { 
                                viewModel.setFilters(cls, selectedSubject)
                                authViewModel.updateSelectedGrade("Grade ${cls.dropLast(2)}")
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
                    
                    items(subjects) { subject ->
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
                    title = { Text("Books") },
                    actions = {
                        IconButton(onClick = { rootNavController.navigate("global_search") }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        if (selectedTabIndex == 0) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = { rootNavController.navigate("global_search") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search books...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("All Books") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Downloads") }
                    )
                }

                if (selectedTabIndex == 0) {
                    if (books.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("No books found for selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                            if (newBooks.isNotEmpty()) {
                                item {
                                    Text("New Books", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(newBooks) { book ->
                                            PlayStoreBookItem(book = book, onBookClick = {
                                                val urlToOpen = offlineBooks.find { it.id == book.id }?.let { "file://" + it.localPdfPath } ?: book.pdfUrl
                                                val encodedUrl = java.net.URLEncoder.encode(urlToOpen, "UTF-8")
                                                rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                                            })
                                        }
                                    }
                                }
                            }
                            booksBySubject.forEach { (subject, subjectBooks) ->
                                item {
                                    Text(
                                        text = subject,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                                    )
                                }
                                items(subjectBooks) { book ->
                                    PlayStoreBookListItem(book = book, onBookClick = {
                                        val urlToOpen = offlineBooks.find { it.id == book.id }?.let { "file://" + it.localPdfPath } ?: book.pdfUrl
                                        val encodedUrl = java.net.URLEncoder.encode(urlToOpen, "UTF-8")
                                        rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                                    })
                                }
                            }
                        }
                    }
                } else {
                    if (offlineBooks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("No downloaded books yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                            offlineBooksBySubject.forEach { (subject, subjectBooks) ->
                                item {
                                    Text(
                                        text = subject,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                                    )
                                }
                                items(subjectBooks) { book ->
                                    PlayStoreBookListItem(book = book, onBookClick = {
                                        val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                                        rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

