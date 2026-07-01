package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Banner
import com.example.data.models.Book
import com.example.data.models.Video
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AuraRepository) : ViewModel() {
    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    private val _recentBooks = MutableStateFlow<List<Book>>(emptyList())
    val recentBooks: StateFlow<List<Book>> = _recentBooks.asStateFlow()

    private val _recentVideos = MutableStateFlow<List<Video>>(emptyList())
    val recentVideos: StateFlow<List<Video>> = _recentVideos.asStateFlow()

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _allBooks.asStateFlow()

    private val _allVideos = MutableStateFlow<List<Video>>(emptyList())
    val allVideos: StateFlow<List<Video>> = _allVideos.asStateFlow()

    private val _selectedGrade = MutableStateFlow<String>("All Grades")
    val selectedGrade: StateFlow<String> = _selectedGrade.asStateFlow()

    init {
        fetchData()
    }

    fun setSelectedGrade(grade: String) {
        _selectedGrade.value = grade
        filterContent(grade)
    }

    private fun filterContent(grade: String) {
        if (grade == "All Grades") {
            _recentBooks.value = _allBooks.value.sortedByDescending { it.createdAt }.take(5)
            _recentVideos.value = _allVideos.value.sortedByDescending { it.createdAt }.take(5)
        } else {
            val gradeStr = grade.replace("Grade ", "")
            val className = when (gradeStr) {
                "1" -> "1st"
                "2" -> "2nd"
                "3" -> "3rd"
                else -> "${gradeStr}th"
            }
            _recentBooks.value = _allBooks.value.filter { it.className == className }.sortedByDescending { it.createdAt }.take(5)
            _recentVideos.value = _allVideos.value.filter { it.className == className }.sortedByDescending { it.createdAt }.take(5)
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            _banners.value = repository.getBanners()
            val fetchedBooks = repository.getBooks()
            _allBooks.value = fetchedBooks
            val fetchedVideos = repository.getVideos()
            _allVideos.value = fetchedVideos
            filterContent(_selectedGrade.value)
        }
    }
}
