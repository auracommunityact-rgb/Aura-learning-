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
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()

    private val _selectedClass = MutableStateFlow<String?>(null)
    val selectedClass: StateFlow<String?> = _selectedClass.asStateFlow()

    init {
        fetchVideos(null)
    }

    fun fetchVideos(className: String?) {
        _selectedClass.value = className
        viewModelScope.launch {
            if (className == null) {
                _videos.value = repository.getVideos()
            } else {
                _videos.value = repository.getVideosByClass(className)
            }
        }
    }
}
