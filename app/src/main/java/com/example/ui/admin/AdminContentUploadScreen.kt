package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.models.Book
import com.example.data.models.Video

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContentUploadScreen(navController: NavController, isVideo: Boolean) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var contentUrl by remember { mutableStateOf("") } // PDF URL or YouTube URL
    var teacher by remember { mutableStateOf("") } // For videos

    var showPreviewDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isVideo) "Upload Video" else "Upload Book") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class / Grade") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (isVideo) {
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Teacher Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            OutlinedTextField(
                value = thumbnailUrl,
                onValueChange = { thumbnailUrl = it },
                label = { Text(if (isVideo) "Thumbnail URL" else "Cover Image URL") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = contentUrl,
                onValueChange = { contentUrl = it },
                label = { Text(if (isVideo) "YouTube Video ID or URL" else "PDF Document URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = { showPreviewDialog = true },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Filled.Preview, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Preview Mode")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = { /* TODO finalize upload */ },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Filled.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Finalize Upload")
                }
            }
        }
    }

    if (showPreviewDialog) {
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = { Text("App Preview") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text("This is how it will appear to users in the app:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isVideo) {
                        // Simulate VideoCard
                        PreviewVideoCard(
                            Video(
                                title = title.ifEmpty { "Sample Video Title" },
                                subject = subject.ifEmpty { "Sample Subject" },
                                teacher = teacher.ifEmpty { "Sample Teacher" },
                                thumbnail = thumbnailUrl
                            )
                        )
                    } else {
                        // Simulate Book Card
                        PreviewBookCard(
                            Book(
                                bookName = title.ifEmpty { "Sample Book Title" },
                                subject = subject.ifEmpty { "Sample Subject" },
                                className = className.ifEmpty { "Sample Class" },
                                coverImage = thumbnailUrl
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPreviewDialog = false }) {
                    Text("Looks Good")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreviewDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PreviewVideoCard(video: Video) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = video.thumbnail.ifEmpty { "https://images.unsplash.com/photo-1596496050827-8299e0220de1?auto=format&fit=crop&w=300&q=80" },
                    contentDescription = video.title,
                    modifier = Modifier.height(200.dp).fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                if (video.title.contains("lesson 1", ignoreCase = true)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart)
                    ) {
                        Text(
                            text = "Lesson 1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Video",
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(video.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(video.description.ifEmpty { "This is a sample description for the preview mode to show how the text will wrap and appear." }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Grade ${video.className}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun PreviewBookCard(book: Book) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=400&auto=format&fit=crop" },
                    contentDescription = book.bookName,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(book.bookName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text("${book.className} • ${book.subject}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
