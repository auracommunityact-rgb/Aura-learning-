package com.example.ui.home
import io.github.jan.supabase.postgrest.from

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.utils.VoiceSearchHelper

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.ViewModelFactory
import java.net.URLEncoder
import kotlinx.coroutines.delay

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.example.ui.study.allStudyTools
import com.example.ui.study.StudyTool
import com.example.ui.profile.BoardResult
import com.example.ui.profile.boardsJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    navController: NavController,
    rootNavController: NavController,
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory),
    initialQuery: String = ""
) {
    val aiSearchResults by viewModel.aiSearchResults.collectAsState()

    val allBooks by viewModel.allBooks.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()

    var websites by remember { mutableStateOf<List<com.example.data.models.Website>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            websites = com.example.data.supabase.SupabaseService.client.from("websites").select().decodeList<com.example.data.models.Website>()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var searchQuery by remember { mutableStateOf(initialQuery) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            delay(500)
            viewModel.fetchAiSearchResults(searchQuery)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val stopWords = remember {
        setOf(
            "ki", "ko", "se", "ka", "ke", "in", "the", "a", "an", "for", "with", "and", "or", "of", "to", "on", "at", "by", "is", "are", "am", "hai", "bhi", "me", "liye", "tha", "the", "he", "she", "it", "they", "we", "you", "i"
        )
    }

    val repository = remember { com.example.data.repository.AuraRepository() }
    var dynamicBoards by remember { mutableStateOf<List<BoardResult>>(emptyList()) }

    LaunchedEffect(Unit) {
        com.example.data.repository.AuraRepository.boardsUpdateTrigger.collect {
            dynamicBoards = repository.getExamBoards()
        }
    }

    val staticBoards = remember {
        try {
            val type = object : TypeToken<List<BoardResult>>() {}.type
            Gson().fromJson<List<BoardResult>>(boardsJson, type)
        } catch (e: Exception) {
            emptyList<BoardResult>()
        }
    }

    val boards = remember(staticBoards, dynamicBoards) {
        dynamicBoards + staticBoards
    }

    fun extractGradeKeywords(words: List<String>): List<String> {
        val grades = mutableListOf<String>()
        for (word in words) {
            val numeric = word.filter { it.isDigit() }
            if (numeric.isNotEmpty()) {
                grades.add(numeric)
                val suffix = when (numeric) {
                    "1" -> "1st"
                    "2" -> "2nd"
                    "3" -> "3rd"
                    else -> "${numeric}th"
                }
                grades.add(suffix)
            }
            if (word == "tenth") grades.addAll(listOf("10", "10th"))
            if (word == "ninth") grades.addAll(listOf("9", "9th"))
            if (word == "eleventh") grades.addAll(listOf("11", "11th"))
            if (word == "twelfth") grades.addAll(listOf("12", "12th"))
        }
        return grades
    }

    // Filtered lists with advanced search scoring
    val searchResults = remember(searchQuery, allBooks, allVideos, boards, websites) {
        if (searchQuery.isBlank()) {
            return@remember SearchResultsWrapper(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }

        val lowercaseQuery = searchQuery.lowercase()
        val words = lowercaseQuery.split(Regex("[\\s\\p{Punct}]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() && !stopWords.contains(it) }

        val activeWords = if (words.isEmpty()) listOf(lowercaseQuery) else words
        val gradeKeywords = extractGradeKeywords(activeWords)

        // 1. Score Books
        val scoredBooks = allBooks.map { book ->
            var score = 0
            for (word in activeWords) {
                if (book.bookName.contains(word, ignoreCase = true)) score += 15
                if (book.className.contains(word, ignoreCase = true)) score += 10
                if (book.subject.contains(word, ignoreCase = true)) score += 12
            }
            val bookIntentWords = listOf("book", "books", "kitab", "pustak", "pdf", "read", "notes", "ncert")
            if (bookIntentWords.any { lowercaseQuery.contains(it) }) {
                score += 5
            }
            for (gk in gradeKeywords) {
                if (book.className.contains(gk, ignoreCase = true)) {
                    score += 15
                }
            }
            book to score
        }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }

        // 2. Score Videos
        val scoredVideos = allVideos.map { video ->
            var score = 0
            for (word in activeWords) {
                if (video.title.contains(word, ignoreCase = true)) score += 15
                if (video.description.contains(word, ignoreCase = true)) score += 5
                if (video.className.contains(word, ignoreCase = true)) score += 10
                if (video.subject.contains(word, ignoreCase = true)) score += 12
                if (video.teacher.contains(word, ignoreCase = true)) score += 10
            }
            val videoIntentWords = listOf("video", "videos", "lecture", "lectures", "play", "watch", "youtube", "tutorial", "explain")
            if (videoIntentWords.any { lowercaseQuery.contains(it) }) {
                score += 5
            }
            for (gk in gradeKeywords) {
                if (video.className.contains(gk, ignoreCase = true)) {
                    score += 15
                }
            }
            video to score
        }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }

        // 3. Score Study Tools
        val scoredTools = allStudyTools.map { tool ->
            var score = 0
            for (word in activeWords) {
                if (tool.title.contains(word, ignoreCase = true)) score += 15
                if (tool.description.contains(word, ignoreCase = true)) score += 8
                if (tool.id.contains(word, ignoreCase = true)) score += 10
            }
            val toolIntentWords = listOf("tool", "tools", "utility", "app", "helper", "solver", "generator", "tracker")
            if (toolIntentWords.any { lowercaseQuery.contains(it) }) {
                score += 5
            }
            tool to score
        }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }

        // 4. Score Exam Result boards
        val scoredBoards = boards.map { board ->
            var score = 0
            for (word in activeWords) {
                if (board.board.contains(word, ignoreCase = true)) score += 15
                if (board.website.contains(word, ignoreCase = true)) score += 5
            }
            val resultIntentWords = listOf("result", "results", "board", "exam", "exams", "website", "link", "portal", "check", "site")
            if (resultIntentWords.any { lowercaseQuery.contains(it) }) {
                score += 10
            }
            board to score
        }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }

        // 5. Score Websites
        val scoredWebsites = websites.map { website ->
            var score = 0
            for (word in activeWords) {
                if (website.name.contains(word, ignoreCase = true)) score += 15
                if (website.description.contains(word, ignoreCase = true)) score += 10
                if (website.url.contains(word, ignoreCase = true)) score += 5
            }
            val websiteIntentWords = listOf("website", "site", "portal", "link", "web")
            if (websiteIntentWords.any { lowercaseQuery.contains(it) }) {
                score += 8
            }
            website to score
        }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }

        SearchResultsWrapper(scoredBooks, scoredVideos, scoredTools, scoredBoards, scoredWebsites)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp).focusRequester(focusRequester),
                        placeholder = { Text("Search books, videos, tools, exams...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        trailingIcon = {
                            val context = LocalContext.current
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
                                }
                            }

                            Row {
                                IconButton(onClick = {
                                    launcher.launch(android.Manifest.permission.RECORD_AUDIO)
                                }) {
                                    Icon(Icons.Filled.Mic, contentDescription = "Voice Search")
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                                    }
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        val hasResults = searchResults.books.isNotEmpty() || 
                         searchResults.videos.isNotEmpty() || 
                         searchResults.tools.isNotEmpty() || 
                         searchResults.boards.isNotEmpty()

        if (searchQuery.isBlank()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Search books, videos, tools and exam result websites", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (!hasResults) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No results found for \"$searchQuery\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                // AI Search Results Section
                if (!aiSearchResults.isNullOrBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AI Search Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = aiSearchResults!!,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Books Results Section
                if (searchResults.books.isNotEmpty()) {
                    item {
                        Text("Books", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    items(searchResults.books) { book ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                rootNavController.navigate("book_detail/${book.id}")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = book.coverImage.ifEmpty { "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=300&q=80" },
                                    contentDescription = book.bookName,
                                    modifier = Modifier.size(60.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(book.bookName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text("Grade ${book.className} • ${book.subject}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Filled.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                
                // Videos Results Section
                if (searchResults.videos.isNotEmpty()) {
                    item {
                        if (searchResults.books.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                        Text("Videos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    items(searchResults.videos) { video ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                rootNavController.navigate("video_player/${video.id}")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = video.thumbnail,
                                    contentDescription = video.title,
                                    modifier = Modifier.size(60.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(video.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(video.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    Text("Grade ${video.className} • ${video.subject} • ${video.teacher}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Study Tools Results Section
                if (searchResults.tools.isNotEmpty()) {
                    item {
                        if (searchResults.books.isNotEmpty() || searchResults.videos.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                        Text("Study Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    items(searchResults.tools) { tool ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
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
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = tool.title,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tool.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text(tool.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                }
                                Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Exam Result websites Results Section
                if (searchResults.boards.isNotEmpty()) {
                    item {
                        if (searchResults.books.isNotEmpty() || searchResults.videos.isNotEmpty() || searchResults.tools.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                        Text("Exam Results Websites", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    items(searchResults.boards) { board ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val encodedUrl = URLEncoder.encode(board.website, "UTF-8")
                                val encodedTitle = URLEncoder.encode(board.board, "UTF-8")
                                rootNavController.navigate("exam_webview?url=${encodedUrl}&title=${encodedTitle}")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = board.board.take(1),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(board.board, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text(board.website, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Uploaded Websites Results Section
                if (searchResults.websites.isNotEmpty()) {
                    item {
                        if (searchResults.books.isNotEmpty() || searchResults.videos.isNotEmpty() || searchResults.tools.isNotEmpty() || searchResults.boards.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                        Text("Websites", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    items(searchResults.websites) { website ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val encodedUrl = URLEncoder.encode(website.url, "UTF-8")
                                val encodedTitle = URLEncoder.encode(website.name, "UTF-8")
                                rootNavController.navigate("exam_webview?url=${encodedUrl}&title=${encodedTitle}")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = website.logo,
                                        contentDescription = website.name,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(website.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(website.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = website.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = website.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

data class SearchResultsWrapper(
    val books: List<com.example.data.models.Book>,
    val videos: List<com.example.data.models.Video>,
    val tools: List<com.example.ui.study.StudyTool>,
    val boards: List<com.example.ui.profile.BoardResult>,
    val websites: List<com.example.data.models.Website>
)
