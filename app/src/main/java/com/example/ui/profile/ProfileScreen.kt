package com.example.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.ThemeViewModel
import com.example.ui.study.allStudyTools
import com.example.ui.study.ToolCard
import androidx.compose.foundation.lazy.grid.items
import kotlinx.coroutines.launch

class ProfileViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel, rootNavController: NavController, themeViewModel: ThemeViewModel? = null) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context.applicationContext))
    
    val profileName by profileViewModel.profileName.collectAsState()
    val profilePictureUri by profileViewModel.profilePictureUri.collectAsState()

    var showEditNameSheet by remember { mutableStateOf(false) }
    
    // Use auth user name as fallback if local profile name is empty
    val isGuest = currentUser == null || currentUser?.id == "guest_user"
    val displayUserName = if (isGuest) "" else (profileName.ifBlank { currentUser?.name ?: "User" })

    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val file = java.io.File(context.filesDir, "profile_picture.jpg")
                        val outputStream = java.io.FileOutputStream(file)
                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()
                        
                        profileViewModel.updateProfilePictureUri(file.absolutePath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Fallback to direct URI if copying fails
                        try {
                            val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            context.contentResolver.takePersistableUriPermission(uri, flag)
                            profileViewModel.updateProfilePictureUri(uri.toString())
                        } catch (e2: Exception) {
                            profileViewModel.updateProfilePictureUri(uri.toString())
                        }
                    }
                }
            }
        }
    )

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { rootNavController.navigate("profile_settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (isGuest) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Welcome Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Welcome to Aura Learning",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Sign in to sync your books, videos, progress, and learning data.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Button(
                            onClick = { rootNavController.navigate("login") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Login", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = { rootNavController.navigate("register") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Sign Up", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image with Edit Badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AnimatedContent(
                                targetState = profilePictureUri,
                                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                                label = "profile_image"
                            ) { uri ->
                                if (uri.isNotEmpty()) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "User Profile Picture",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = displayUserName.take(1).uppercase(),
                                            style = MaterialTheme.typography.displayMedium,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            // Edit Badge
                            SmallFloatingActionButton(
                                onClick = {
                                    imagePickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier.offset(x = 8.dp, y = 8.dp),
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Edit Picture", modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Name and Edit Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (displayUserName.isNotEmpty()) {
                                Text(
                                    text = displayUserName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (!isGuest) {
                                IconButton(
                                    onClick = { showEditNameSheet = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit Name", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        
                        if (currentUser != null && currentUser?.email?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentUser!!.email,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Additional Sections (like ExamResultCard)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MyLibraryCard(rootNavController)
                ExamResultCard(rootNavController)
                MyToolsSection(rootNavController)
            }
        }
    }

    if (showEditNameSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditNameSheet = false }
        ) {
            EditNameContent(
                initialName = displayUserName,
                onSave = { newName ->
                    profileViewModel.updateProfileName(newName)
                    showEditNameSheet = false
                },
                onCancel = { showEditNameSheet = false }
            )
        }
    }
}

@Composable
fun EditNameContent(initialName: String, onSave: (String) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf(initialName) }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Edit Name", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = {
                if (it.length <= 50) {
                    text = it
                    isError = it.trim().isEmpty()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true,
            isError = isError,
            supportingText = {
                if (isError) Text("Name cannot be empty")
                else Text("${text.length}/50")
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (text.trim().isNotEmpty()) {
                        onSave(text.trim())
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun ExamResultCard(rootNavController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { rootNavController.navigate("exam_results") },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("🎓", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Check Exam Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "View your official board exam results.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun MyToolsSection(rootNavController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "My Tools",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(300.dp)
        ) {
            items(allStudyTools.take(4)) { tool ->
                ToolCard(tool = tool) {
                    when (tool.id) {
                        "planner" -> rootNavController.navigate("study_planner")
                        "countdown" -> rootNavController.navigate("exam_countdown")
                        "pdf_reader" -> rootNavController.navigate("pdf_tool")
                        "map_agent" -> rootNavController.navigate("map_agent")
                        "translate" -> rootNavController.navigate("notes_translate")
                        "calculator" -> rootNavController.navigate("calculator")
                        "result_analysis" -> rootNavController.navigate("result_analysis")
                        "progress" -> rootNavController.navigate("progress")
                        "weekly_report" -> rootNavController.navigate("weekly_report")
                        "ai_homework" -> rootNavController.navigate("ai_chat?prompt=" + android.net.Uri.encode("Hey Gemini AI! I need help with my homework. Can you help me solve it step-by-step and explain the core concepts clearly?"))
                        "ai_doubt" -> rootNavController.navigate("ai_chat?prompt=" + android.net.Uri.encode("Hi Gemini AI! I have a specific doubt in my syllabus. Can you clarify it with clean explanations and examples?"))
                        "ai_summarizer" -> rootNavController.navigate("ai_chat?prompt=" + android.net.Uri.encode("Hello! Can you help me summarize this educational topic or text into concise, high-yield revision notes?"))
                        "ai_essay" -> rootNavController.navigate("ai_chat?prompt=" + android.net.Uri.encode("Hi! Can you guide me in writing or structuring a polished academic essay on my topic?"))
                        "ai_mcq" -> rootNavController.navigate("ai_chat?prompt=" + android.net.Uri.encode("Hey Gemini! Can you generate a set of practice Multiple Choice Questions (MCQs) on my topic with answers and brief explanations?"))
                    }
                }
            }
        }
    }
}

@Composable
fun MyLibraryCard(rootNavController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { rootNavController.navigate("my_library") },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text("📚", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "My Learning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Access your saved books and video lessons.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
