import re
with open('app/src/main/java/com/example/ui/home/HomeViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('private val _allCourses = MutableStateFlow<List<com.example.data.models.Course>>(emptyList())', 'private val _allQuestionPapers = MutableStateFlow<List<com.example.data.models.QuestionPaper>>(emptyList())')
content = content.replace('val allCourses: StateFlow<List<com.example.data.models.Course>> = _allCourses.asStateFlow()', 'val allQuestionPapers: StateFlow<List<com.example.data.models.QuestionPaper>> = _allQuestionPapers.asStateFlow()')

content = content.replace('val fetchedCourses = repository.getCourses()', 'val fetchedQuestionPapers = repository.getQuestionPapers()')
content = content.replace('_allCourses.value = fetchedCourses', '_allQuestionPapers.value = fetchedQuestionPapers')

with open('app/src/main/java/com/example/ui/home/HomeViewModel.kt', 'w') as f:
    f.write(content)
