    // Courses
    suspend fun getCourses(): List<Course> {
        return try {
            client.postgrest["courses"].select().decodeList<Course>()
        } catch (e: Exception) {
            emptyList()
        }
    }
