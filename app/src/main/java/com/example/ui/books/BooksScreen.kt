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
import com.example.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(navController: NavController, viewModel: BooksViewModel = viewModel(factory = ViewModelFactory)) {
    val books by viewModel.books.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()

    val classes = listOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th")

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
                Tab(selected = selectedClass == null, onClick = { viewModel.fetchBooks(null) }, text = { Text("All") })
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
                items(books) { book ->
                    val context = LocalContext.current
                    Card(modifier = Modifier.fillMaxWidth().height(200.dp).clickable { 
                        if (book.pdfUrl.isNotEmpty()) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                            intent.setDataAndType(android.net.Uri.parse(book.pdfUrl), "application/pdf")
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "No PDF viewer found", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Column {
                            AsyncImage(
                                model = book.coverImage,
                                contentDescription = book.bookName,
                                modifier = Modifier.height(140.dp).fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                            Text(book.bookName, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            Text(book.className, modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

