awk '
/private fun fetchCourses\(\)/ {
    print "    fun fetchCourses() {"
    next
}
{ print }
' app/src/main/java/com/example/ui/courses/CourseViewModel.kt > temp.kt
mv temp.kt app/src/main/java/com/example/ui/courses/CourseViewModel.kt
