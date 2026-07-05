package com.example.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Book
import com.example.data.models.Video
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: AuraRepository = AuraRepository()) : ViewModel() {
    
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers: StateFlow<Int> = _totalUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    init {
        loadData()
    }

    fun clearError() {
        _errorMsg.value = null
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _books.value = repository.getBooks()
                _videos.value = repository.getVideos()
                _totalUsers.value = repository.getUsersCount()
            } catch (e: Exception) {
                _errorMsg.value = "Failed to load admin data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addBook(book)
                loadData()
            } catch (e: Exception) {
                _errorMsg.value = "Failed to add book: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateBook(book)
                loadData()
            } catch (e: Exception) {
                _errorMsg.value = "Failed to update book: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteBook(bookId)
                loadData()
            } catch (e: Exception) {
                _errorMsg.value = "Failed to delete book: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun addVideo(video: Video) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addVideo(video)
                loadData()
            } catch (e: Exception) {
                _errorMsg.value = "Failed to add video: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateVideo(video: Video) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateVideo(video)
                loadData()
            } catch (e: Exception) {
                _errorMsg.value = "Failed to update video: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteVideo(videoId)
                loadData()
            } catch (e: Exception) {
                _errorMsg.value = "Failed to delete video: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
