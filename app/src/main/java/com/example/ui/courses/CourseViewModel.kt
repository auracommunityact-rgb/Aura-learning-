package com.example.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: AuraRepository) : ViewModel() {
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchCourses()
    }

    private fun fetchCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            // Fetch courses from Supabase
            val fetchedCourses = repository.getCourses()
            _courses.value = fetchedCourses
            _isLoading.value = false
        }
    }
}
