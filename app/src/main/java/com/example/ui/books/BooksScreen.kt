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
import androidx.compose.material.icons.outlined.BookmarkBorder
import com.example.ui.auth.AuthViewModel

import com.example.ui.ViewModelFactory
import androidx.compose.foundation.lazy.grid.GridItemSpan
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems

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
    val offlineBooksList = offlineBooks.map { 
        com.example.data.models.Book(
            id = it.id,
            bookName = it.bookName,
            className = it.className,
            subject = it.subject,
            coverImage = it.coverImage,
            pdfUrl = "file://" + it.localPdfPath
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
                Divider()
                
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
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    lazyItems(classes) { cls ->
                        NavigationDrawerItem(
                            label = { Text(cls) },
                            selected = selectedClass == cls,
                            onClick = { 
                                viewModel.setFilters(cls, selectedSubject)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
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
                    
                    lazyItems(subjects) { subject ->
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
                            booksBySubject.forEach { (subject, subjectBooks) ->
                                item {
                                    Text(
                                        text = subject,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 16.dp, top = if (subject == booksBySubject.keys.first()) 16.dp else 24.dp, bottom = 8.dp)
                                    )
                                    DigitalBookshelf(
                                        books = subjectBooks,
                                        savedBookIds = currentUser?.savedBooks ?: emptyList(),
                                        offlineBookIds = offlineBooks.map { it.id },
                                        downloadProgress = downloadProgress,
                                        onBookClick = { book ->
                                            if (book.pdfUrl.isNotEmpty()) {
                                                val urlToOpen = offlineBooks.find { it.id == book.id }?.let { "file://" + it.localPdfPath } ?: book.pdfUrl
                                                val encodedUrl = java.net.URLEncoder.encode(urlToOpen, "UTF-8")
                                                rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                                            }
                                        },
                                        onToggleSave = { bookId ->
                                            if (currentUser == null) {
                                                showLoginPrompt = true
                                            } else {
                                                authViewModel.toggleSaveBook(bookId)
                                            }
                                        },
                                        onDownloadBook = { book -> offlineBooksViewModel.downloadBook(book) },
                                        onDeleteOfflineBook = { bookId -> offlineBooksViewModel.deleteOfflineBook(bookId) }
                                    )
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
                                        modifier = Modifier.padding(start = 16.dp, top = if (subject == offlineBooksBySubject.keys.first()) 16.dp else 24.dp, bottom = 8.dp)
                                    )
                                    DigitalBookshelf(
                                        books = subjectBooks,
                                        savedBookIds = currentUser?.savedBooks ?: emptyList(),
                                        offlineBookIds = offlineBooks.map { it.id },
                                        downloadProgress = downloadProgress,
                                        onBookClick = { book ->
                                            val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                                            rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                                        },
                                        onToggleSave = { bookId ->
                                            if (currentUser == null) {
                                                showLoginPrompt = true
                                            } else {
                                                authViewModel.toggleSaveBook(bookId)
                                            }
                                        },
                                        onDownloadBook = { book -> offlineBooksViewModel.downloadBook(book) },
                                        onDeleteOfflineBook = { bookId -> offlineBooksViewModel.deleteOfflineBook(bookId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

