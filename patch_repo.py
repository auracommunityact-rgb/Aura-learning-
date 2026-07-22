import re

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

# Replace Course with QuestionPaper in list
content = content.replace('client.postgrest["courses"].select().decodeList<Course>()', 'client.postgrest["question_papers"].select().decodeList<QuestionPaper>()')
content = content.replace('android.util.Log.e("AuraRepository", "Error fetching courses", e)', 'android.util.Log.e("AuraRepository", "Error fetching question papers", e)')

content = content.replace('suspend fun addCourse(course: Course) {', 'suspend fun addQuestionPaper(paper: QuestionPaper) {')
content = content.replace('val newCourse = if (course.id.isEmpty()) course.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else course', 'val newPaper = if (paper.id.isEmpty()) paper.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else paper')
content = content.replace('client.postgrest["courses"].insert(newCourse)', 'client.postgrest["question_papers"].insert(newPaper)')

content = content.replace('suspend fun updateCourse(course: Course) {', 'suspend fun updateQuestionPaper(paper: QuestionPaper) {')
content = content.replace('if (course.id.isNotEmpty()) {', 'if (paper.id.isNotEmpty()) {')
content = content.replace('client.postgrest["courses"].update(course) {', 'client.postgrest["question_papers"].update(paper) {')
content = content.replace('filter { eq("id", course.id) }', 'filter { eq("id", paper.id) }')

content = content.replace('suspend fun deleteCourse(courseId: String) {', 'suspend fun deleteQuestionPaper(paperId: String) {')
content = content.replace('if (courseId.isNotEmpty()) {', 'if (paperId.isNotEmpty()) {')
content = content.replace('val idValue: Any = courseId.toLongOrNull() ?: courseId', 'val idValue: Any = paperId.toLongOrNull() ?: paperId')
content = content.replace('client.postgrest["courses"].delete {', 'client.postgrest["question_papers"].delete {')

# Realtime
content = content.replace('val channel = client.realtime.channel("courses-updates")', 'val channel = client.realtime.channel("question-papers-updates")')
content = content.replace('table = "courses"', 'table = "question_papers"')

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'w') as f:
    f.write(content)
