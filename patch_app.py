import re
with open('app/src/main/java/com/example/AuraLearningApp.kt', 'r') as f:
    content = f.read()

content = content.replace('Course', 'QuestionPaper')
content = content.replace('course', 'questionPaper')
content = content.replace('courses', 'questionPapers')

with open('app/src/main/java/com/example/AuraLearningApp.kt', 'w') as f:
    f.write(content)
