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
import androidx.compose.material.icons.outlined.BookmarkBorder
import com.example.ui.auth.AuthViewModel

import com.example.ui.ViewModelFactory
import androidx.compose.foundation.lazy.grid.GridItemSpan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(navController: NavController, authViewModel: AuthViewModel, viewModel: BooksViewModel = viewModel(factory = ViewModelFactory)) {
    val books by viewModel.books.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val classes = listOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th")
    val booksBySubject = books.groupBy { it.subject.ifEmpty { "Other" } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Books") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = if (selectedClass == null) 0 else classes.indexOf(selectedClass) + 1,
                edgePadding = 16.dp
            ) {
                Tab(selected = selectedClass == null, onClick = { viewModel.fetchBooks(null) }, text = { Text("All Grades") })
                classes.forEach { cls ->
                    Tab(
                        selected = selectedClass == cls,
                        onClick = { viewModel.fetchBooks(cls) },
                        text = { Text(cls) }
                    )
                }
            }

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
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                    intent.setDataAndType(android.net.Uri.parse(book.pdfUrl), "application/pdf")
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "No PDF viewer found", android.widget.Toast.LENGTH_SHORT).show()
                                    }
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
                                        onClick = { authViewModel.toggleSaveBook(book.id) },
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

