package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.User
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserSearchViewModel(private val repository: AuraRepository = AuraRepository()) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        setupSearch()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // 300ms Debounce to prevent excessive API hits
                .distinctUntilChanged()
                .onEach { query ->
                    if (query.length >= 2) {
                        _isLoading.value = true
                    }
                }
                .collect { query ->
                    if (query.length >= 2) {
                        _searchResults.value = repository.searchUsers(query)
                        _isLoading.value = false
                    } else {
                        _searchResults.value = emptyList()
                        _isLoading.value = false
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
