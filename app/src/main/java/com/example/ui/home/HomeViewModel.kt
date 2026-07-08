package com.example.ui.home

import androidx.lifecycle.ViewModel
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.auth.auth
import androidx.lifecycle.viewModelScope
import com.example.data.models.Banner
import com.example.data.models.Book
import com.example.data.models.Video
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject

class HomeViewModel(private val repository: AuraRepository) : ViewModel() {
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    private val _recentBooks = MutableStateFlow<List<Book>>(emptyList())
    val recentBooks: StateFlow<List<Book>> = _recentBooks.asStateFlow()

    private val _recentVideos = MutableStateFlow<List<Video>>(emptyList())
    val recentVideos: StateFlow<List<Video>> = _recentVideos.asStateFlow()
    private val _continueWatching = MutableStateFlow<List<Video>>(emptyList())
    val continueWatching: StateFlow<List<Video>> = _continueWatching.asStateFlow()
    private val _continueReading = MutableStateFlow<List<Book>>(emptyList())
    val continueReading: StateFlow<List<Book>> = _continueReading.asStateFlow()

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _allBooks.asStateFlow()

    private val _allVideos = MutableStateFlow<List<Video>>(emptyList())
    val allVideos: StateFlow<List<Video>> = _allVideos.asStateFlow()

    private val _selectedGrade = MutableStateFlow<String>("All Grades")
    val selectedGrade: StateFlow<String> = _selectedGrade.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String>("All Subjects")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _aiSearchResults = MutableStateFlow<String?>(null)
    val aiSearchResults: StateFlow<String?> = _aiSearchResults.asStateFlow()

    fun fetchAiSearchResults(query: String) {
        viewModelScope.launch {
            try {
                // Need to use the API key from BuildConfig
                val apiKey = BuildConfig.GEMINI_API_KEY
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = query)))),
                    tools = listOf(
                        // Create the google_search tool object
                        // Based on Gemini API docs, this is typically {"google_search": {}}
                        buildJsonObject {
                            putJsonObject("google_search") {}
                        }
                    )
                )
                val response = RetrofitClient.service.generateContent(apiKey, request)
                _aiSearchResults.value = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            } catch (e: Exception) {
                _aiSearchResults.value = "Error fetching AI results: ${e.message}"
            }
        }
    }

    private val _activeExamSubject = MutableStateFlow<String?>(null)
    val activeExamSubject: StateFlow<String?> = _activeExamSubject.asStateFlow()

    init {
        viewModelScope.launch {
            merge(
                AuraRepository.booksUpdateTrigger,
                AuraRepository.videosUpdateTrigger
            ).collect {
                fetchData()
            }
        }
    }

    fun setSelectedGrade(grade: String) {
        _selectedGrade.value = grade
        filterContent(_selectedGrade.value, _selectedSubject.value)
    }

    fun setSelectedSubject(subject: String) {
        _selectedSubject.value = subject
        filterContent(_selectedGrade.value, _selectedSubject.value)
    }

    fun setActiveExamSubject(subject: String?) {
        _activeExamSubject.value = subject
        filterContent(_selectedGrade.value, _selectedSubject.value)
    }

    private fun filterContent(grade: String, subject: String) {
        var filteredBooks = _allBooks.value
        var filteredVideos = _allVideos.value

        if (grade != "All Grades") {
            val gradeStr = grade.replace("Grade ", "")
            val isNumeric = gradeStr.any { it.isDigit() }
            if (isNumeric && gradeStr.isNotEmpty()) {
                val cleanGrade = gradeStr.filter { it.isDigit() }
                val className = when (cleanGrade) {
                    "1" -> "1st"
                    "2" -> "2nd"
                    "3" -> "3rd"
                    else -> "${cleanGrade}th"
                }
                filteredBooks = filteredBooks.filter { it.className.equals(className, ignoreCase = true) }
                filteredVideos = filteredVideos.filter { it.className.equals(className, ignoreCase = true) }
            }
        }

        val activeExam = _activeExamSubject.value
        if (!activeExam.isNullOrBlank()) {
            filteredBooks = filteredBooks.filter { it.subject.equals(activeExam, ignoreCase = true) }
            filteredVideos = filteredVideos.filter { it.subject.equals(activeExam, ignoreCase = true) }
        } else if (subject != "All Subjects" && subject.isNotEmpty()) {
            val mappedSubject = when (subject) {
                "SST" -> "Social Studies"
                "Computer" -> "Computer Science"
                else -> subject
            }
            filteredBooks = filteredBooks.filter { it.subject.equals(mappedSubject, ignoreCase = true) }
            filteredVideos = filteredVideos.filter { it.subject.equals(mappedSubject, ignoreCase = true) }
        }

        _recentBooks.value = filteredBooks.sortedByDescending { it.createdAt }.take(5)
        _recentVideos.value = filteredVideos.sortedByDescending { it.createdAt }.take(5)
    }

    private fun fetchData() {
        viewModelScope.launch {
            _banners.value = repository.getBanners()
            val fetchedBooks = repository.getBooks()
            _allBooks.value = fetchedBooks
            val fetchedVideos = repository.getVideos()
            _allVideos.value = fetchedVideos
            filterContent(_selectedGrade.value, _selectedSubject.value)
            
            // Load user progress
            val userId = SupabaseService.client.auth.currentSessionOrNull()?.user?.id
            if (userId != null) {
                val videoProgress = repository.getVideoProgress(userId)
                val bookProgress = repository.getBookProgress(userId)
                
                val cVideos = videoProgress
                    .sortedByDescending { it.lastWatchedAt }
                    .mapNotNull { vp -> fetchedVideos.find { it.id == vp.videoId } }
                    .take(5)
                _continueWatching.value = cVideos
                
                val cBooks = bookProgress
                    .sortedByDescending { it.lastReadAt }
                    .mapNotNull { bp -> fetchedBooks.find { it.id == bp.bookId } }
                    .take(5)
                _continueReading.value = cBooks
            }
        }
    }
}
