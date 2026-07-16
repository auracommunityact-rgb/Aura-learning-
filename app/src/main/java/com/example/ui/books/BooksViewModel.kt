package com.example.ui.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Book
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BooksViewModel(private val repository: AuraRepository) : ViewModel() {
    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _selectedClass = MutableStateFlow<String?>(null)
    val selectedClass: StateFlow<String?> = _selectedClass.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String?>(null)
    val selectedSubject: StateFlow<String?> = _selectedSubject.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchBooks()
        viewModelScope.launch {
            AuraRepository.booksUpdateTrigger.collect {
                fetchBooks()
            }
        }
    }

    fun fetchBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allBooks.value = repository.getBooks()
                applyFilters()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilters(className: String?, subject: String?) {
        _selectedClass.value = className
        _selectedSubject.value = subject
        applyFilters()
    }

    private fun applyFilters() {
        val cls = _selectedClass.value
        val sub = _selectedSubject.value
        var filtered = _allBooks.value

        if (cls != null) {
            filtered = filtered.filter { it.className.equals(cls, ignoreCase = true) }
        }
        if (sub != null && sub.isNotEmpty()) {
            val mappedSubject = when (sub) {
                "SST" -> "Social Studies"
                "Computer" -> "Computer Science"
                else -> sub
            }
            filtered = filtered.filter { it.subject.equals(mappedSubject, ignoreCase = true) }
        }

        _books.value = filtered
    }
}
