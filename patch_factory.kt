        if (modelClass.isAssignableFrom(com.example.ui.courses.CourseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.example.ui.courses.CourseViewModel(repository) as T
        }
