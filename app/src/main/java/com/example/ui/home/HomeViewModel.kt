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
            val className = when (gradeStr) {
                "1" -> "1st"
                "2" -> "2nd"
                "3" -> "3rd"
                else -> "${gradeStr}th"
            }
            filteredBooks = filteredBooks.filter { it.className == className }
            filteredVideos = filteredVideos.filter { it.className == className }
        }

        val activeExam = _activeExamSubject.value
        if (!activeExam.isNullOrBlank()) {
            filteredBooks = filteredBooks.filter { it.subject.equals(activeExam, ignoreCase = true) }
            filteredVideos = filteredVideos.filter { it.subject.equals(activeExam, ignoreCase = true) }
        } else if (subject != "All Subjects") {
            filteredBooks = filteredBooks.filter { it.subject.equals(subject, ignoreCase = true) }
            filteredVideos = filteredVideos.filter { it.subject.equals(subject, ignoreCase = true) }
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
