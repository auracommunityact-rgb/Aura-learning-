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
            if (fetchedCourses.isNotEmpty()) {
                _courses.value = fetchedCourses
            } else {
                // Fallback to some defaults if table doesn't exist or is empty
                _courses.value = listOf(
                    Course("1", "Mathematics", "Advanced Mathematics", "Learn algebra, geometry, and calculus with our comprehensive math course.", "https://images.unsplash.com/photo-1509228468518-180dd4864904?auto=format&fit=crop&q=80&w=400"),
                    Course("2", "Science", "General Science", "Explore physics, chemistry, and biology in this foundational science course.", "https://images.unsplash.com/photo-1532094349884-543bc11b234d?auto=format&fit=crop&q=80&w=400"),
                    Course("3", "History", "World History", "Journey through time and understand the events that shaped our world.", "https://images.unsplash.com/photo-1461360370896-922624d12aa1?auto=format&fit=crop&q=80&w=400")
                )
            }
            _isLoading.value = false
        }
    }
}
