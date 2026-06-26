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
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _selectedClass = MutableStateFlow<String?>(null)
    val selectedClass: StateFlow<String?> = _selectedClass.asStateFlow()

    init {
        fetchBooks(null)
    }

    fun fetchBooks(className: String?) {
        _selectedClass.value = className
        viewModelScope.launch {
            if (className == null) {
                _books.value = repository.getBooks()
            } else {
                _books.value = repository.getBooksByClass(className)
            }
        }
    }
}
