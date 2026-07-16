sed -i 's/course.thumbnailUrl.ifEmpty/course.thumbnailUrl?.ifEmpty/g' app/src/main/java/com/example/ui/courses/CourseListingScreen.kt
sed -i 's/course.title/course.title ?: ""/g' app/src/main/java/com/example/ui/courses/CourseListingScreen.kt
sed -i 's/course.subject/course.subject ?: ""/g' app/src/main/java/com/example/ui/courses/CourseListingScreen.kt
sed -i 's/contentDescription = course.title ?: "",/contentDescription = course.title,/g' app/src/main/java/com/example/ui/courses/CourseListingScreen.kt
