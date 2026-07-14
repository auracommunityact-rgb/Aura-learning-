package com.example.ui.admin

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.models.Course
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCourseUploadScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AuraRepository() }
    var isUploading by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    
    var thumbnailUrl by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var contentFileUrl by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            coroutineScope.launch {
                isUploading = true
                try {
                    val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                    bytes?.let { b ->
                        thumbnailUrl = repository.uploadCoverImage(b, "course_cover_${System.currentTimeMillis()}.jpg")
                    }
                } finally {
                    isUploading = false
                }
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            coroutineScope.launch {
                isUploading = true
                try {
                    val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                    bytes?.let { b ->
                        contentFileUrl = repository.uploadBookPdf(b, "course_file_${System.currentTimeMillis()}.pdf")
                    }
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Course") },
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
            
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Text(if (thumbnailUrl.isEmpty()) "Pick Cover Image" else "Image Picked")
                }
                if (thumbnailUrl.isNotEmpty()) {
                    Text("Image Uploaded", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
                }
            }
            
            OutlinedTextField(
                value = youtubeUrl,
                onValueChange = { youtubeUrl = it },
                label = { Text("YouTube Video URL") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { filePicker.launch("application/pdf") }, modifier = Modifier.weight(1f)) {
                    Text(if (contentFileUrl.isEmpty()) "Pick Content File (PDF)" else "File Picked")
                }
                if (contentFileUrl.isNotEmpty()) {
                    Text("File Uploaded", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (title.isBlank() || subject.isBlank()) {
                        Toast.makeText(context, "Please fill Title and Subject", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        isUploading = true
                        try {
                            val course = Course(
                                title = title,
                                description = description,
                                subject = subject,
                                thumbnailUrl = thumbnailUrl,
                                youtubeUrl = youtubeUrl,
                                contentFileUrl = contentFileUrl,
                                createdAt = System.currentTimeMillis()
                            )
                            repository.addCourse(course)
                            Toast.makeText(context, "Course uploaded successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Filled.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload Course")
                }
            }
        }
    }
}
