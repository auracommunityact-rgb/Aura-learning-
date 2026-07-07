package com.example.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.auth.AuthViewModel

data class StudyTool(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isAi: Boolean = false
)

val allStudyTools = listOf(
    StudyTool("notes_reader", "Notes Reader", "Read digital notes with beautiful UI", Icons.AutoMirrored.Filled.MenuBook),
    StudyTool("pdf_reader", "PDF Reader", "Open and read PDF books", Icons.Filled.PictureAsPdf),
    StudyTool("pdf_highlighter", "PDF Highlighter", "Highlight and annotate PDFs", Icons.Filled.BorderColor),
    StudyTool("ocr_scanner", "OCR Text Scanner", "Extract text from images", Icons.Filled.DocumentScanner),
    StudyTool("translate", "Translate Notes", "Translate study material", Icons.Filled.Translate),
    StudyTool("tts", "Text To Speech", "Listen to your notes aloud", Icons.Filled.RecordVoiceOver),
    StudyTool("calculator", "Scientific Calculator", "Solve mathematical calculations", Icons.Filled.Calculate),
    StudyTool("planner", "Study Planner", "Organize your study schedule", Icons.Filled.CalendarMonth),
    StudyTool("pomodoro", "Pomodoro Timer", "Increase study focus", Icons.Filled.Timer),
    StudyTool("todo", "To-Do List", "Manage daily study tasks", Icons.Filled.Checklist),
    StudyTool("countdown", "Exam Countdown", "Track remaining exam days", Icons.Filled.Event),
    StudyTool("goal_tracker", "Goal Tracker", "Track learning goals", Icons.Filled.TrackChanges),
    StudyTool("quiz_gen", "Quiz Generator", "Generate quizzes for practice", Icons.Filled.Quiz),
    StudyTool("mock_tests", "Mock Tests", "Practice complete exams", Icons.AutoMirrored.Filled.Assignment),
    StudyTool("result_analysis", "Result Analysis", "Analyze performance", Icons.Filled.Analytics),
    StudyTool("progress", "Progress Tracker", "Track study performance", Icons.AutoMirrored.Filled.TrendingUp),
    StudyTool("map_agent", "Map Agent", "Information about places & routes", Icons.Filled.Map),
    StudyTool("ai_homework", "AI Homework Helper", "Owner AI: Help solve homework", Icons.Filled.SmartToy, true),
    StudyTool("ai_doubt", "AI Doubt Solver", "Owner AI: Solve student doubts", Icons.Filled.QuestionAnswer, true),
    StudyTool("ai_summarizer", "AI Notes Summarizer", "Owner AI: Create short notes", Icons.Filled.Summarize, true),
    StudyTool("ai_essay", "AI Essay Writer", "Owner AI: Write essays", Icons.Filled.Description, true),
    StudyTool("ai_flashcard", "AI Flashcard Gen", "Owner AI: Generate flashcards", Icons.Filled.Style, true),
    StudyTool("ai_mcq", "AI MCQ Generator", "Owner AI: Generate MCQs", Icons.AutoMirrored.Filled.ListAlt, true),
    StudyTool("video_lectures", "Video Lectures", "Watch educational videos", Icons.Filled.VideoLibrary),
    StudyTool("ncert", "NCERT Books", "Read NCERT books online", Icons.AutoMirrored.Filled.LibraryBooks),
    StudyTool("prev_papers", "Previous Papers", "Practice past exams", Icons.Filled.HistoryEdu),
    StudyTool("sample_papers", "Sample Papers", "Practice model papers", Icons.Filled.Science),
    StudyTool("syllabus", "Syllabus Viewer", "View latest syllabus", Icons.AutoMirrored.Filled.Subject),
    StudyTool("notebook", "Digital Notebook", "Create personal notes", Icons.Filled.NoteAlt),
    StudyTool("drawing_pad", "Drawing Pad", "Draw diagrams", Icons.Filled.Draw),
    StudyTool("doc_scanner", "Document Scanner", "Scan documents to PDF", Icons.Filled.Scanner),
    StudyTool("cloud_backup", "Cloud Backup", "Backup user data securely", Icons.Filled.CloudUpload),
    StudyTool("download_manager", "Download Manager", "Manage your downloads", Icons.Filled.Download),
    StudyTool("study_streak", "Study Streak", "Increase consistency", Icons.Filled.LocalFireDepartment),
    StudyTool("achievements", "Achievements", "Unlock badges & rewards", Icons.Filled.WorkspacePremium),
    StudyTool("weekly_report", "Weekly Report", "Weekly performance report", Icons.Filled.Assessment),
    StudyTool("rewards", "Rewards System", "Redeem rewards & coins", Icons.Filled.CardGiftcard)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    rootNavController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredTools = remember(searchQuery) {
        if (searchQuery.isBlank()) allStudyTools
        else allStudyTools.filter { it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Tools", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search 36+ tools...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTools) { tool ->
                    ToolCard(tool = tool) {
                        if (tool.id == "planner") {
                            rootNavController.navigate("study_planner")
                        } else if (tool.id == "countdown") {
                            navController.navigate("exam_countdown")
                        } else if (tool.id == "pdf_reader") {
                            rootNavController.navigate("pdf_tool")
                        } else if (tool.id == "map_agent") {
                            rootNavController.navigate("map_agent")
                        } else if (tool.id == "translate") {
                            rootNavController.navigate("notes_translate")
                        } else if (tool.id == "calculator") {
                            rootNavController.navigate("calculator")
                        } else if (tool.id == "result_analysis") {
                            rootNavController.navigate("result_analysis")
                        } else if (tool.id == "progress") {
                            rootNavController.navigate("progress")
                        } else if (tool.id == "weekly_report") {
                            rootNavController.navigate("weekly_report")
                        } else {
                            rootNavController.navigate("tool_viewer/${tool.id}?title=${tool.title}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolCard(tool: StudyTool, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.title,
                modifier = Modifier.size(40.dp),
                tint = if (tool.isAi) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
