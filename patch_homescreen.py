import re
with open('app/src/main/java/com/example/ui/home/HomeScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Course', 'QuestionPaper')
content = content.replace('course', 'questionPaper')
content = content.replace('thumbnailUrl', 'thumbnail')
# Also we need to check if any specific methods exist for course that were renamed

with open('app/src/main/java/com/example/ui/home/HomeScreen.kt', 'w') as f:
    f.write(content)
