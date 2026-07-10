package com.example.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.models.Course
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditCourseScreen(navController: NavController, courseId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AuraRepository() }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }
    var contentFileUrl by remember { mutableStateOf("") }

    // Load course details
    LaunchedEffect(courseId) {
        coroutineScope.launch {
            isLoading = true
            try {
                val courses = repository.getCourses()
                val course = courses.find { it.id == courseId }
                if (course != null) {
                    title = course.title
                    description = course.description
                    subject = course.subject
                    thumbnailUrl = course.thumbnailUrl
                    youtubeUrl = course.youtubeUrl
                    contentFileUrl = course.contentFileUrl
                } else {
                    Toast.makeText(context, "Course not found", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Course") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                
                OutlinedTextField(
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it },
                    label = { Text("Cover Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("YouTube Video URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = contentFileUrl,
                    onValueChange = { contentFileUrl = it },
                    label = { Text("Content File URL (PDF/Other)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (title.isBlank() || subject.isBlank()) {
                            Toast.makeText(context, "Please fill Title and Subject", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            isSaving = true
                            try {
                                val course = Course(
                                    id = courseId,
                                    title = title,
                                    description = description,
                                    subject = subject,
                                    thumbnailUrl = thumbnailUrl,
                                    youtubeUrl = youtubeUrl,
                                    contentFileUrl = contentFileUrl,
                                    createdAt = System.currentTimeMillis()
                                )
                                repository.updateCourse(course)
                                Toast.makeText(context, "Course updated successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
