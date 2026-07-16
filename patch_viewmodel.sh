awk '
/init \{/ {
    print "    init {"
    print "        fetchCourses()"
    print "        repository.subscribeToCourses {"
    print "            fetchCourses()"
    print "        }"
    skip = 1
    next
}
skip == 1 && /\}/ {
    skip = 0
    next
}
skip == 1 { next }
{ print }
' app/src/main/java/com/example/ui/courses/CourseViewModel.kt > temp.kt
mv temp.kt app/src/main/java/com/example/ui/courses/CourseViewModel.kt
