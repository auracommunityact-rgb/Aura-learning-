package com.example.ui.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Book
import com.example.data.models.Video
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoPlayerViewModel(private val repository: AuraRepository) : ViewModel() {

    private val _video = MutableStateFlow<Video?>(null)
    val video: StateFlow<Video?> = _video.asStateFlow()

    private val _chapterVideos = MutableStateFlow<List<Video>>(emptyList())
    val chapterVideos: StateFlow<List<Video>> = _chapterVideos.asStateFlow()

    private val _relatedBooks = MutableStateFlow<List<Book>>(emptyList())
    val relatedBooks: StateFlow<List<Book>> = _relatedBooks.asStateFlow()

    private val _suggestedVideos = MutableStateFlow<List<Video>>(emptyList())
    val suggestedVideos: StateFlow<List<Video>> = _suggestedVideos.asStateFlow()

    fun loadVideo(videoId: String) {
        viewModelScope.launch {
            val v = repository.getVideoById(videoId)
            _video.value = v
            if (v != null) {
                // Load other parts of the chapter
                val allVideos = repository.getVideos()
                _chapterVideos.value = allVideos.filter { 
                    it.chapter.equals(v.chapter, ignoreCase = true) && 
                    it.subject.equals(v.subject, ignoreCase = true) &&
                    it.className.equals(v.className, ignoreCase = true)
                }.sortedBy { it.partNumber }

                // Load related books
                val allBooks = repository.getBooksByClass(v.className)
                _relatedBooks.value = allBooks.filter { 
                    v.relatedBooks.contains(it.id) || it.subject.equals(v.subject, ignoreCase = true)
                }

                // Load suggested videos
                _suggestedVideos.value = allVideos.filter {
                    it.id != v.id && it.className == v.className && it.subject == v.subject
                }
            }
        }
    }
}
