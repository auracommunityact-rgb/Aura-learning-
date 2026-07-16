package com.example.ui.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.ViewModelFactory
import com.example.ui.profile.BoardResult
import com.example.ui.profile.boardsJson
import com.example.ui.study.StudyTool
import com.example.ui.study.allStudyTools
import com.example.utils.VoiceSearchHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.jan.supabase.postgrest.from
import java.net.URLEncoder
import kotlinx.coroutines.delay

data class HistoryItem(
    val query: String,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class LocalAiAnswer(
    val content: String,
    val relatedBooks: List<com.example.data.models.Book>,
    val relatedVideos: List<com.example.data.models.Video>,
    val relatedCourses: List<com.example.data.models.Course>
)

val offlineCourses = listOf(
    com.example.data.models.Course(
        id = "course_maths_10",
        subject = "Maths",
        title = "Class 10 CBSE Maths Masterclass",
        description = "Complete syllabus for Class 10th Mathematics covering Real Numbers, Polynomials, Trigonometry, and Statistics with solved exercises.",
        thumbnailUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?auto=format&fit=crop&w=300&q=80",
        youtubeUrl = "https://www.youtube.com/watch?v=N4tL7mG_C-o"
    ),
    com.example.data.models.Course(
        id = "course_science_10",
        subject = "Science",
        title = "Class 10 Science: Concept Booster",
        description = "Learn critical Physics, Chemistry, and Biology concepts for Class 10 with interactive animations, chemical equations, and practice papers.",
        thumbnailUrl = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=300&q=80",
        youtubeUrl = "https://www.youtube.com/watch?v=f9vT8H_7gK8"
    ),
    com.example.data.models.Course(
        id = "course_sst_10",
        subject = "Social Studies",
        title = "Class 10 Social Science Quick Revision",
        description = "A rapid revision course for Class 10 Board exams in History, Geography, Political Science, and Economics, featuring major event timelines.",
        thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=300&q=80",
        youtubeUrl = "https://www.youtube.com/watch?v=3Sg9OWhn3bM"
    )
)

fun scoreBook(book: com.example.data.models.Book, query: String): Int {
    val q = query.lowercase().trim()
    if (q.isEmpty()) return 0
    val title = book.bookName.lowercase()
    val subject = book.subject.lowercase()
    val grade = book.className.lowercase()

    var score = 0
    if (title == q) score += 500
    else if (title.startsWith(q)) score += 300
    else if (title.contains(q)) score += 150

    if (subject == q) score += 200
    else if (subject.startsWith(q)) score += 100
    else if (subject.contains(q)) score += 50

    if (grade == q || grade.contains(q)) score += 80
    return score
}

fun scoreVideo(video: com.example.data.models.Video, query: String): Int {
    val q = query.lowercase().trim()
    if (q.isEmpty()) return 0
    val title = video.title.lowercase()
    val description = video.description.lowercase()
    val subject = video.subject.lowercase()
    val grade = video.className.lowercase()
    val teacher = video.teacher.lowercase()

    var score = 0
    if (title == q) score += 500
    else if (title.startsWith(q)) score += 300
    else if (title.contains(q)) score += 150

    if (description.contains(q)) score += 60

    if (subject == q) score += 200
    else if (subject.startsWith(q)) score += 100
    else if (subject.contains(q)) score += 50

    if (grade == q || grade.contains(q)) score += 80
    if (teacher.contains(q)) score += 100
    return score
}

fun scoreCourse(course: com.example.data.models.Course, query: String): Int {
    val q = query.lowercase().trim()
    if (q.isEmpty()) return 0
    val title = course.title.lowercase()
    val description = course.description.lowercase()
    val subject = course.subject.lowercase()

    var score = 0
    if (title == q) score += 500
    else if (title.startsWith(q)) score += 300
    else if (title.contains(q)) score += 150

    if (description.contains(q)) score += 60

    if (subject == q) score += 200
    else if (subject.startsWith(q)) score += 100
    else if (subject.contains(q)) score += 50
    return score
}

fun scoreTool(tool: com.example.ui.study.StudyTool, query: String): Int {
    val q = query.lowercase().trim()
    if (q.isEmpty()) return 0
    val title = tool.title.lowercase()
    val description = tool.description.lowercase()

    var score = 0
    if (title == q) score += 500
    else if (title.startsWith(q)) score += 300
    else if (title.contains(q)) score += 150

    if (description.contains(q)) score += 60
    return score
}

fun scoreBoard(board: com.example.ui.profile.BoardResult, query: String): Int {
    val q = query.lowercase().trim()
    if (q.isEmpty()) return 0
    val name = board.board.lowercase()
    val website = board.website.lowercase()

    var score = 0
    if (name == q) score += 500
    else if (name.startsWith(q)) score += 300
    else if (name.contains(q)) score += 150

    if (website.contains(q)) score += 60
    return score
}

fun scoreWebsite(website: com.example.data.models.Website, query: String): Int {
    val q = query.lowercase().trim()
    if (q.isEmpty()) return 0
    val name = website.name.lowercase()
    val desc = website.description.lowercase()
    val url = website.url.lowercase()

    var score = 0
    if (name == q) score += 500
    else if (name.startsWith(q)) score += 300
    else if (name.contains(q)) score += 150

    if (desc.contains(q)) score += 60
    if (url.contains(q)) score += 40
    return score
}

fun generateLocalAiAnswer(
    query: String,
    books: List<com.example.data.models.Book>,
    videos: List<com.example.data.models.Video>,
    courses: List<com.example.data.models.Course>
): LocalAiAnswer? {
    val q = query.lowercase().trim()
    if (q.isBlank()) return null

    if (q.contains("math") || q.contains("ganit") || q.contains("trig") || q.contains("algebra") || q.contains("geometry")) {
        return LocalAiAnswer(
            content = "Mathematics is the study of numbers, quantities, shapes, and logical reasoning. In Class 10, it covers core branches like Algebra (Polynomials, Quadratic Equations), Geometry (Triangles, Circles), Trigonometry, coordinate systems, and Statistics/Probability. Practice and active derivation are the best ways to build fluency.",
            relatedBooks = books.filter { it.subject.lowercase().contains("math") || it.bookName.lowercase().contains("math") },
            relatedVideos = videos.filter { it.subject.lowercase().contains("math") || it.title.lowercase().contains("math") },
            relatedCourses = courses.filter { it.subject.lowercase().contains("math") || it.title.lowercase().contains("math") }
        )
    }

    if (q.contains("science") || q.contains("vigyan") || q.contains("phys") || q.contains("chem") || q.contains("bio") || q.contains("carbon") || q.contains("acid")) {
        return LocalAiAnswer(
            content = "Science integrates the study of Physics (motion, electricity, optics), Chemistry (reactions, periodic tables, organic compounds), and Biology (life processes, heredity, environment). Developing clear conceptual understanding, revising chemical equations, and practice labeling diagrams are key study practices.",
            relatedBooks = books.filter { it.subject.lowercase().contains("sci") || it.bookName.lowercase().contains("sci") },
            relatedVideos = videos.filter { it.subject.lowercase().contains("sci") || it.title.lowercase().contains("sci") },
            relatedCourses = courses.filter { it.subject.lowercase().contains("sci") || it.title.lowercase().contains("sci") }
        )
    }

    if (q.contains("kritika") || q.contains("hindi") || q.contains("kshitij") || q.contains("sparsh") || q.contains("sanchayan")) {
        return LocalAiAnswer(
            content = "Kritika is a supplementary Hindi textbook for Class 9 & 10 secondary education. It features classic literary stories by iconic Indian writers that promote empathy, historical consciousness, and deep linguistic appreciation. Focus on chapter summaries, character arcs, and thematic meanings to score well.",
            relatedBooks = books.filter { it.bookName.lowercase().contains("kritika") || it.bookName.lowercase().contains("hindi") },
            relatedVideos = videos.filter { it.title.lowercase().contains("kritika") || it.title.lowercase().contains("hindi") },
            relatedCourses = courses.filter { it.title.lowercase().contains("hindi") }
        )
    }

    if (q.contains("result") || q.contains("rajasthan") || q.contains("rbse") || q.contains("cbse") || q.contains("board") || q.contains("exam")) {
        return LocalAiAnswer(
            content = "Rajasthan Board of Secondary Education (RBSE) and CBSE exam results are released officially on state result portals. Aura Learning provides direct result webviews so you can input your Roll Number and access your official marksheet immediately upon release.",
            relatedBooks = books.filter { it.className.contains("10") },
            relatedVideos = videos.filter { it.title.lowercase().contains("exam") || it.title.lowercase().contains("result") },
            relatedCourses = emptyList()
        )
    }

    if (q.contains("pdf") || q.contains("reader") || q.contains("notes")) {
        return LocalAiAnswer(
            content = "Aura Learning's built-in PDF Reader enables students to read, analyze, and translate any academic NCERT books or custom notes offline. You can easily click any textbook item in the Search results to launch the reader immediately.",
            relatedBooks = books.take(3),
            relatedVideos = emptyList(),
            relatedCourses = emptyList()
        )
    }

    val matchedBooks = books.filter { scoreBook(it, query) > 50 }
    val matchedVideos = videos.filter { scoreVideo(it, query) > 50 }
    val matchedCourses = courses.filter { scoreCourse(it, query) > 50 }

    if (matchedBooks.isNotEmpty() || matchedVideos.isNotEmpty() || matchedCourses.isNotEmpty()) {
        val firstBook = matchedBooks.firstOrNull()
        val firstVideo = matchedVideos.firstOrNull()
        val subjectName = firstBook?.subject ?: firstVideo?.subject ?: "your selected topics"
        return LocalAiAnswer(
            content = "Found highly relevant learning material in $subjectName inside Aura Learning. We've matched ${matchedBooks.size} textbooks, ${matchedVideos.size} videos, and ${matchedCourses.size} video masterclasses matching your search query. Start exploring them below!",
            relatedBooks = matchedBooks.take(2),
            relatedVideos = matchedVideos.take(2),
            relatedCourses = matchedCourses.take(2)
        )
    }
    return null
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Website" -> Icons.Default.School
        "Book" -> Icons.Default.Book
        "Video" -> Icons.Default.PlayCircle
        "Course" -> Icons.Default.School
        "Tool" -> Icons.Default.Build
        else -> Icons.Default.Search
    }
}

@Composable
fun GoogleSearchCard(
    category: String,
    categoryIcon: ImageVector,
    displayPath: String,
    title: String,
    description: String,
    thumbnailUrl: String?,
    gradeText: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$category • $displayPath",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!thumbnailUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            if (!gradeText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF334155),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = gradeText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LocalAiAnswerCard(
    query: String,
    books: List<com.example.data.models.Book>,
    videos: List<com.example.data.models.Video>,
    courses: List<com.example.data.models.Course>,
    onBookClick: (String) -> Unit,
    onVideoClick: (String) -> Unit,
    onCourseClick: (com.example.data.models.Course) -> Unit
) {
    val localAnswer = remember(query, books, videos, courses) {
        generateLocalAiAnswer(query, books, videos, courses)
    }

    if (localAnswer == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📘 AI Answer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("No AI answer available.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📘 AI Answer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0369A1)
                ) {
                    Text(
                        text = "Local Model",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = localAnswer.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF1F5F9),
                lineHeight = 22.sp
            )
            
            if (localAnswer.relatedBooks.isNotEmpty() || localAnswer.relatedVideos.isNotEmpty() || localAnswer.relatedCourses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(12.dp))
                
                if (localAnswer.relatedBooks.isNotEmpty()) {
                    Text("Related Books", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(8.dp))
                    localAnswer.relatedBooks.take(2).forEach { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookClick(book.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Book: ${book.bookName} (${book.subject})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (localAnswer.relatedVideos.isNotEmpty()) {
                    Text("Related Videos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(8.dp))
                    localAnswer.relatedVideos.take(2).forEach { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVideoClick(video.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Video: ${video.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (localAnswer.relatedCourses.isNotEmpty()) {
                    Text("Related Courses", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(8.dp))
                    localAnswer.relatedCourses.take(2).forEach { course ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCourseClick(course) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Course: ${course.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    navController: NavController,
    rootNavController: NavController,
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory),
    initialQuery: String = ""
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val allBooks by viewModel.allBooks.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()

    var courses by remember { mutableStateOf<List<com.example.data.models.Course>>(emptyList()) }
    var websites by remember { mutableStateOf<List<com.example.data.models.Website>>(emptyList()) }

    val repository = remember { com.example.data.repository.AuraRepository() }
    var dynamicBoards by remember { mutableStateOf<List<BoardResult>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            websites = com.example.data.supabase.SupabaseService.client.from("websites").select().decodeList<com.example.data.models.Website>()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            courses = repository.getCourses()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        com.example.data.repository.AuraRepository.boardsUpdateTrigger.collect {
            dynamicBoards = repository.getExamBoards()
        }
    }

    val staticBoards = remember {
        try {
            val type = object : TypeToken<List<BoardResult>>() {}.type
            Gson().fromJson<List<BoardResult>>(boardsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList<BoardResult>()
        }
    }

    val boards = remember(staticBoards, dynamicBoards) {
        dynamicBoards + staticBoards
    }

    val allCoursesList = remember(courses) {
        offlineCourses + courses
    }

    // Search and Suggestion State
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var isSearchConfirmed by remember { mutableStateOf(initialQuery.isNotBlank()) }

    // History Preference management
    val sharedPreferences = remember(context) {
        context.getSharedPreferences("aura_search_prefs", Context.MODE_PRIVATE)
    }

    var historyList by remember {
        mutableStateOf<List<HistoryItem>>(emptyList())
    }

    fun loadHistory(): List<HistoryItem> {
        val json = sharedPreferences.getString("history_list", null)
        if (json.isNullOrBlank()) {
            return listOf(
                HistoryItem("Maths", isPinned = false),
                HistoryItem("Class 10 Science", isPinned = false),
                HistoryItem("Kritika", isPinned = false),
                HistoryItem("Rajasthan Result", isPinned = false),
                HistoryItem("PDF Reader", isPinned = false)
            )
        }
        return try {
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            Gson().fromJson<List<HistoryItem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    LaunchedEffect(Unit) {
        historyList = loadHistory()
        if (initialQuery.isBlank()) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    fun saveHistory(newList: List<HistoryItem>) {
        val sortedList = newList.sortedWith(
            compareByDescending<HistoryItem> { it.isPinned }
                .thenByDescending { it.timestamp }
        )
        historyList = sortedList
        val json = Gson().toJson(sortedList)
        sharedPreferences.edit().putString("history_list", json).apply()
    }

    fun addToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val existing = historyList.find { it.query.equals(trimmed, ignoreCase = true) }
        val newList = historyList.filterNot { it.query.equals(trimmed, ignoreCase = true) }.toMutableList()
        
        val newItem = HistoryItem(
            query = existing?.query ?: trimmed,
            isPinned = existing?.isPinned ?: false,
            timestamp = System.currentTimeMillis()
        )
        newList.add(0, newItem)
        
        val cappedList = if (newList.size > 20) {
            val unpinned = newList.filter { !it.isPinned }
            if (unpinned.isNotEmpty()) {
                val oldestUnpinned = unpinned.minByOrNull { it.timestamp }
                newList.filterNot { it == oldestUnpinned }
            } else {
                newList.take(20)
            }
        } else {
            newList
        }
        saveHistory(cappedList)
    }

    fun togglePin(item: HistoryItem) {
        val newList = historyList.map {
            if (it.query == item.query) {
                it.copy(isPinned = !it.isPinned, timestamp = System.currentTimeMillis())
            } else {
                it
            }
        }
        saveHistory(newList)
    }

    fun deleteHistoryItem(item: HistoryItem) {
        val newList = historyList.filterNot { it.query == item.query }
        saveHistory(newList)
    }

    fun clearAllHistory() {
        saveHistory(emptyList())
    }

    // Speech Recognition Setup
    val voiceHelper = remember { VoiceSearchHelper(context) }
    val speechResult by voiceHelper.speechResult.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceHelper.startListening()
        }
    }

    LaunchedEffect(speechResult) {
        if (speechResult.isNotEmpty()) {
            searchQuery = speechResult
            isSearchConfirmed = true
            addToHistory(speechResult)
            focusManager.clearFocus()
            voiceHelper.clearSpeechResult()
        }
    }

    // Dynamic suggestions based on query
    val suggestions = remember(searchQuery, allBooks, allVideos, allCoursesList, boards, websites, historyList) {
        if (searchQuery.isBlank()) return@remember emptyList<String>()
        val q = searchQuery.lowercase().trim()
        val result = mutableSetOf<String>()

        // 1. Local history
        historyList.forEach { item ->
            if (item.query.lowercase().contains(q)) {
                result.add(item.query)
            }
        }

        // 2. Predefined smart educational queries
        val smartPredefined = listOf(
            "Maths", "Math Formula", "Math Video", "Math Notes", "Math Question Bank", "Math Sample Paper", "Math Result",
            "Class 10 Science", "Class 10 Physics", "Class 10 Chemistry", "Class 10 Board Result", "Science Notes", "Science Question Bank",
            "Kritika", "Kritika Class 10 Notes", "Kritika PDF Book",
            "Rajasthan Result", "RBSE 10th Result", "CBSE Board Result",
            "PDF Reader", "PDF Reader Tool", "PDF Notes Translator",
            "Study Planner", "Exam Countdown", "Translator"
        )
        smartPredefined.forEach { s ->
            if (s.lowercase().contains(q)) {
                result.add(s)
            }
        }

        // 3. Database models
        allBooks.forEach { book ->
            if (book.bookName.lowercase().contains(q)) result.add(book.bookName)
            if (book.subject.lowercase().contains(q)) result.add(book.subject)
        }
        allVideos.forEach { video ->
            if (video.title.lowercase().contains(q)) result.add(video.title)
            if (video.subject.lowercase().contains(q)) result.add(video.subject)
        }
        allCoursesList.forEach { course ->
            if (course.title.lowercase().contains(q)) result.add(course.title)
            if (course.subject.lowercase().contains(q)) result.add(course.subject)
        }
        allStudyTools.forEach { tool ->
            if (tool.title.lowercase().contains(q)) result.add(tool.title)
        }
        boards.forEach { board ->
            if (board.board.lowercase().contains(q)) result.add(board.board)
        }
        websites.forEach { w ->
            if (w.name.lowercase().contains(q)) result.add(w.name)
        }

        result.filter { it.isNotBlank() }
            .sortedWith(compareBy(
                { !it.lowercase().startsWith(q) },
                { it.length }
            ))
            .take(12)
    }

    // Dynamic Search Results scoring and ranking
    val scoredWebsites = remember(searchQuery, websites) {
        websites.map { it to scoreWebsite(it, searchQuery) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    val scoredBoards = remember(searchQuery, boards) {
        boards.map { it to scoreBoard(it, searchQuery) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    val scoredCourses = remember(searchQuery, allCoursesList) {
        allCoursesList.map { it to scoreCourse(it, searchQuery) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    val scoredBooks = remember(searchQuery, allBooks) {
        allBooks.map { it to scoreBook(it, searchQuery) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    val scoredVideos = remember(searchQuery, allVideos) {
        allVideos.map { it to scoreVideo(it, searchQuery) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    val scoredTools = remember(searchQuery) {
        allStudyTools.map { it to scoreTool(it, searchQuery) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    val hasAnyResults = scoredWebsites.isNotEmpty() || scoredBoards.isNotEmpty() || scoredCourses.isNotEmpty() ||
            scoredBooks.isNotEmpty() || scoredVideos.isNotEmpty() || scoredTools.isNotEmpty()

    val onBackAction = {
        if (isSearchConfirmed) {
            isSearchConfirmed = false
        } else {
            navController.popBackStack()
        }
    }

    BackHandler {
        onBackAction()
    }

    // Standard Horizontal Tabs
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Books", "Videos", "Courses", "Result Portals")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Rounded Google-Style Top Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackAction() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(26.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(26.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                isSearchConfirmed = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    isSearchConfirmed = true
                                    addToHistory(searchQuery)
                                    focusManager.clearFocus()
                                }
                            }),
                            cursorBrush = SolidColor(Color(0xFF38BDF8)),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search books, videos, courses...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    isSearchConfirmed = false
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                launcher.launch(android.Manifest.permission.RECORD_AUDIO)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            // Body Area
            if (searchQuery.isEmpty()) {
                // Show Recent Searches / History
                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No recent searches yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                TextButton(
                                    onClick = { clearAllHistory() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Text("Clear All")
                                }
                            }
                        }

                        items(historyList) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (item.isPinned) Color(0xFF1E293B).copy(alpha = 0.5f)
                                        else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        searchQuery = item.query
                                        isSearchConfirmed = true
                                        addToHistory(item.query)
                                        focusManager.clearFocus()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = item.query,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Pin button
                                IconButton(
                                    onClick = { togglePin(item) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = "Pin Favorite",
                                        tint = if (item.isPinned) Color(0xFFFBBF24) else Color(0xFF475569),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                // Delete button
                                IconButton(
                                    onClick = { deleteHistoryItem(item) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Item",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFF1E293B))
                        }
                    }
                }
            } else if (!isSearchConfirmed) {
                // Show instant Search Suggestions
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(suggestions) { suggestion ->
                        val isHistoryItem = remember(historyList, suggestion) {
                            historyList.any { it.query.equals(suggestion, ignoreCase = true) }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = suggestion
                                    isSearchConfirmed = true
                                    addToHistory(suggestion)
                                    focusManager.clearFocus()
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isHistoryItem) Icons.Default.History else Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = {
                                    searchQuery = suggestion
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Fill",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFF1E293B))
                    }
                }
            } else {
                // Search Results Page
                Column(modifier = Modifier.fillMaxSize()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF38BDF8),
                        edgePadding = 16.dp,
                        divider = { HorizontalDivider(color = Color(0xFF1E293B)) }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, fontWeight = FontWeight.Medium) },
                                selectedContentColor = Color(0xFF38BDF8),
                                unselectedContentColor = Color(0xFF64748B)
                            )
                        }
                    }

                    if (!hasAnyResults) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No results found for \"$searchQuery\"",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Suggested searches:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    val fallbackSearches = listOf("Maths", "Class 10 Science", "PDF Reader", "Kritika")
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        fallbackSearches.forEach { fs ->
                                            item {
                                                SuggestionChip(
                                                    onClick = {
                                                        searchQuery = fs
                                                        isSearchConfirmed = true
                                                        addToHistory(fs)
                                                    },
                                                    label = { Text(fs) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = Color(0xFF1E293B),
                                                        labelColor = Color(0xFF38BDF8)
                                                    ),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        Color(0xFF334155)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Render filtered items
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (selectedTab == 0) {
                                // "All" Tab
                                // 1. AI Answer Card
                                item {
                                    LocalAiAnswerCard(
                                        query = searchQuery,
                                        books = allBooks,
                                        videos = allVideos,
                                        courses = allCoursesList,
                                        onBookClick = { id -> rootNavController.navigate("book_detail/$id") },
                                        onVideoClick = { id -> rootNavController.navigate("video_player/$id") },
                                        onCourseClick = { course ->
                                            val encUrl = URLEncoder.encode(course.youtubeUrl, "UTF-8")
                                            val encTitle = URLEncoder.encode(course.title, "UTF-8")
                                            rootNavController.navigate("exam_webview?url=$encUrl&title=$encTitle")
                                        }
                                    )
                                }

                                // 2. Websites
                                val mergedWebsites = (scoredWebsites.map { "website" to it } + scoredBoards.map { "board" to it })
                                if (mergedWebsites.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Websites & Portals",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                        )
                                    }
                                    items(mergedWebsites) { pair ->
                                        if (pair.first == "website") {
                                            val w = pair.second as com.example.data.models.Website
                                            GoogleSearchCard(
                                                category = "Website",
                                                categoryIcon = Icons.Default.Language,
                                                displayPath = w.url.substringAfter("://").substringBefore("/"),
                                                title = w.name,
                                                description = w.description,
                                                thumbnailUrl = w.logo,
                                                gradeText = null,
                                                onClick = {
                                                    val encUrl = URLEncoder.encode(w.url, "UTF-8")
                                                    val encTitle = URLEncoder.encode(w.name, "UTF-8")
                                                    rootNavController.navigate("exam_webview?url=$encUrl&title=$encTitle")
                                                }
                                            )
                                        } else {
                                            val b = pair.second as BoardResult
                                            GoogleSearchCard(
                                                category = "Result Portal",
                                                categoryIcon = Icons.Default.School,
                                                displayPath = b.website.substringAfter("://").substringBefore("/"),
                                                title = b.board,
                                                description = "Official website to view examination results, notifications, and student profiles for ${b.board}.",
                                                thumbnailUrl = null,
                                                gradeText = "Board Exam",
                                                onClick = {
                                                    val encUrl = URLEncoder.encode(b.website, "UTF-8")
                                                    val encTitle = URLEncoder.encode(b.board, "UTF-8")
                                                    rootNavController.navigate("exam_webview?url=$encUrl&title=$encTitle")
                                                }
                                            )
                                        }
                                    }
                                }

                                // 3. Courses
                                if (scoredCourses.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Courses",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                        )
                                    }
                                    items(scoredCourses) { course ->
                                        GoogleSearchCard(
                                            category = "Course",
                                            categoryIcon = Icons.Default.School,
                                            displayPath = "Aura Learning > Courses",
                                            title = course.title,
                                            description = course.description,
                                            thumbnailUrl = course.thumbnailUrl,
                                            gradeText = course.subject,
                                            onClick = {
                                                val encUrl = URLEncoder.encode(course.youtubeUrl, "UTF-8")
                                                val encTitle = URLEncoder.encode(course.title, "UTF-8")
                                                rootNavController.navigate("exam_webview?url=$encUrl&title=$encTitle")
                                            }
                                        )
                                    }
                                }

                                // 4. Books
                                if (scoredBooks.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Books & Notes",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                        )
                                    }
                                    items(scoredBooks) { book ->
                                        GoogleSearchCard(
                                            category = "Book",
                                            categoryIcon = Icons.Default.Book,
                                            displayPath = "Aura Learning > Books > ${book.subject}",
                                            title = book.bookName,
                                            description = "Read full online PDF notes, chapters and syllabus textbooks for Class ${book.className}.",
                                            thumbnailUrl = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                                            gradeText = "Grade ${book.className} • ${book.subject}",
                                            onClick = {
                                                rootNavController.navigate("book_detail/${book.id}")
                                            }
                                        )
                                    }
                                }

                                // 5. Videos
                                if (scoredVideos.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Videos",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                        )
                                    }
                                    items(scoredVideos) { video ->
                                        GoogleSearchCard(
                                            category = "Video",
                                            categoryIcon = Icons.Default.PlayCircle,
                                            displayPath = "Aura Learning > Videos > ${video.subject}",
                                            title = video.title,
                                            description = video.description,
                                            thumbnailUrl = video.thumbnail,
                                            gradeText = "Grade ${video.className} • ${video.subject} • ${video.teacher}",
                                            onClick = {
                                                rootNavController.navigate("video_player/${video.id}")
                                            }
                                        )
                                    }
                                }

                                // 6. Tools
                                if (scoredTools.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Tools",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                        )
                                    }
                                    items(scoredTools) { tool ->
                                        GoogleSearchCard(
                                            category = "Tool",
                                            categoryIcon = getCategoryIcon("Tool"),
                                            displayPath = "Aura Learning > Tools",
                                            title = tool.title,
                                            description = tool.description,
                                            thumbnailUrl = null,
                                            gradeText = if (tool.isAi) "AI Tool" else "Study Tool",
                                            onClick = {
                                                if (tool.id == "planner") {
                                                    rootNavController.navigate("study_planner")
                                                } else if (tool.id == "countdown") {
                                                    rootNavController.navigate("exam_countdown")
                                                } else if (tool.id == "pdf_reader") {
                                                    rootNavController.navigate("pdf_tool")
                                                } else if (tool.id == "map_agent") {
                                                    rootNavController.navigate("map_agent")
                                                } else if (tool.id == "translate") {
                                                    rootNavController.navigate("notes_translate")
                                                } else if (tool.id == "calculator") {
                                                    rootNavController.navigate("calculator")
                                                } else {
                                                    rootNavController.navigate("tool_viewer/${tool.id}?title=${tool.title}")
                                                }
                                            }
                                        )
                                    }
                                }
                            } else if (selectedTab == 1) {
                                // Books Tab
                                items(scoredBooks) { book ->
                                    GoogleSearchCard(
                                        category = "Book",
                                        categoryIcon = Icons.Default.Book,
                                        displayPath = "Aura Learning > Books > ${book.subject}",
                                        title = book.bookName,
                                        description = "Read full online PDF notes, chapters and syllabus textbooks for Class ${book.className}.",
                                        thumbnailUrl = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                                        gradeText = "Grade ${book.className} • ${book.subject}",
                                        onClick = {
                                            rootNavController.navigate("book_detail/${book.id}")
                                        }
                                    )
                                }
                            } else if (selectedTab == 2) {
                                // Videos Tab
                                items(scoredVideos) { video ->
                                    GoogleSearchCard(
                                        category = "Video",
                                        categoryIcon = Icons.Default.PlayCircle,
                                        displayPath = "Aura Learning > Videos > ${video.subject}",
                                        title = video.title,
                                        description = video.description,
                                        thumbnailUrl = video.thumbnail,
                                        gradeText = "Grade ${video.className} • ${video.subject} • ${video.teacher}",
                                        onClick = {
                                            rootNavController.navigate("video_player/${video.id}")
                                        }
                                    )
                                }
                            } else if (selectedTab == 3) {
                                // Courses Tab
                                items(scoredCourses) { course ->
                                    GoogleSearchCard(
                                        category = "Course",
                                        categoryIcon = Icons.Default.School,
                                        displayPath = "Aura Learning > Courses",
                                        title = course.title,
                                        description = course.description,
                                        thumbnailUrl = course.thumbnailUrl,
                                        gradeText = course.subject,
                                        onClick = {
                                            val encUrl = URLEncoder.encode(course.youtubeUrl, "UTF-8")
                                            val encTitle = URLEncoder.encode(course.title, "UTF-8")
                                            rootNavController.navigate("exam_webview?url=$encUrl&title=$encTitle")
                                        }
                                    )
                                }
                            } else if (selectedTab == 4) {
                                // Result Portals Tab
                                items(scoredWebsites) { w ->
                                    GoogleSearchCard(
                                        category = "Website",
                                        categoryIcon = Icons.Default.Language,
                                        displayPath = w.url.substringAfter("://").substringBefore("/"),
                                        title = w.name,
                                        description = w.description,
                                        thumbnailUrl = w.logo,
                                        gradeText = null,
                                        onClick = {
                                            val encUrl = URLEncoder.encode(w.url, "UTF-8")
                                            val encTitle = URLEncoder.encode(w.name, "UTF-8")
                                            rootNavController.navigate("exam_webview?url=$encUrl&title=$encTitle")
                                        }
                                    )
                                }
                                items(scoredBoards) { b ->
                                    GoogleSearchCard(
                                        category = "Result Portal",
                                        categoryIcon = Icons.Default.School,
                                        displayPath = b.website.substringAfter("://").substringBefore("/"),
                                        title = b.board,
                                        description = "Official website to view examination results, notifications, and student profiles for ${b.board}.",
                                        thumbnailUrl = null,
                                        gradeText = "Board Exam",
                                        onClick = {
                                            val encUrl = URLEncoder.encode(b.website, "UTF-8")
                                            val encTitle = URLEncoder.encode(b.board, "UTF-8")
                                            rootNavController.navigate("exam_webview?url=$encUrl&title=$encTitle")
                                        }
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
