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

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _banners.value = repository.getBanners()
            val fetchedBooks = repository.getBooks()
            _allBooks.value = fetchedBooks
            // In a real app we'd limit this or order by createdAt, for now just fetch all
            _recentBooks.value = fetchedBooks.sortedByDescending { it.createdAt }.take(5)
            _recentVideos.value = repository.getVideos().sortedByDescending { it.createdAt }.take(5)
        }
    }
}
