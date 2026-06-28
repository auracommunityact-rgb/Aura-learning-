package com.example.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Book
import com.example.data.models.Video
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _books.value = repository.getBooks()
            _videos.value = repository.getVideos()
            _totalUsers.value = repository.getUsersCount()
            _isLoading.value = false
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addBook(book)
            loadData()
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateBook(book)
            loadData()
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteBook(bookId)
            loadData()
        }
    }

    fun addVideo(video: Video) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addVideo(video)
            loadData()
        }
    }

    fun updateVideo(video: Video) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateVideo(video)
            loadData()
        }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteVideo(videoId)
            loadData()
        }
    }
}
