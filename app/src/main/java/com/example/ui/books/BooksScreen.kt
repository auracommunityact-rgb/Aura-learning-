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
fun BooksScreen(navController: NavController, authViewModel: AuthViewModel, rootNavController: NavController, viewModel: BooksViewModel = viewModel(factory = ViewModelFactory)) {
    val books by viewModel.books.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showLoginPrompt by remember { mutableStateOf(false) }

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
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (books.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No books found for selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        booksBySubject.forEach { (subject, subjectBooks) ->
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = subject,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = if (subject == booksBySubject.keys.first()) 0.dp else 16.dp, bottom = 4.dp)
                                )
                            }
                            items(subjectBooks) { book ->
                                val context = LocalContext.current
                                Card(
                                    modifier = Modifier.fillMaxWidth().height(220.dp).clickable { 
                                        if (book.pdfUrl.isNotEmpty()) {
                                            val encodedUrl = java.net.URLEncoder.encode(book.pdfUrl, "UTF-8")
                                            rootNavController.navigate("pdf_viewer?url=$encodedUrl")
                                        }
                                    },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column {
                                        Box {
                                            AsyncImage(
                                                model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                                                contentDescription = book.bookName,
                                                modifier = Modifier.height(140.dp).fillMaxWidth(),
                                                contentScale = ContentScale.Crop
                                            )
                                            IconButton(
                                                onClick = { 
                                                    if (currentUser == null) {
                                                        showLoginPrompt = true
                                                    } else {
                                                        authViewModel.toggleSaveBook(book.id) 
                                                    }
                                                },
                                                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                                            ) {
                                                val isSaved = currentUser?.savedBooks?.contains(book.id) == true
                                                Icon(
                                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                                    contentDescription = "Save Book",
                                                    tint = if (isSaved) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.White
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(book.bookName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                            Text("Grade ${book.className}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

