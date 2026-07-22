import re

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

content = content.replace('suspend fun getCourses(): List<Course>', 'suspend fun getQuestionPapers(): List<QuestionPaper>')
content = content.replace('fun subscribeToCourses(onUpdate: () -> Unit)', 'fun subscribeToQuestionPapers(onUpdate: () -> Unit)')
content = content.replace('import com.example.data.models.Course', 'import com.example.data.models.QuestionPaper')

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'w') as f:
    f.write(content)
