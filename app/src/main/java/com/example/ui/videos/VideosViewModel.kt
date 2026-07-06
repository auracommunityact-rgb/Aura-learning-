package com.example.ui.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Video
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideosViewModel(private val repository: AuraRepository) : ViewModel() {
    private val _allVideos = MutableStateFlow<List<Video>>(emptyList())
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()

    private val _selectedClass = MutableStateFlow<String?>(null)
    val selectedClass: StateFlow<String?> = _selectedClass.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String?>(null)
    val selectedSubject: StateFlow<String?> = _selectedSubject.asStateFlow()

    init {
        viewModelScope.launch {
            AuraRepository.videosUpdateTrigger.collect {
                fetchVideos()
            }
        }
    }

    private fun fetchVideos() {
        viewModelScope.launch {
            _allVideos.value = repository.getVideos()
            applyFilters()
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
        var filtered = _allVideos.value

        if (cls != null) {
            filtered = filtered.filter { it.className == cls }
        }
        if (sub != null) {
            filtered = filtered.filter { it.subject.equals(sub, ignoreCase = true) }
        }

        _videos.value = filtered
    }
}
