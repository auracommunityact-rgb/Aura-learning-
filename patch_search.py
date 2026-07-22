import re
with open('app/src/main/java/com/example/ui/home/GlobalSearchScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Course', 'QuestionPaper')
content = content.replace('course', 'questionPaper')
content = content.replace('thumbnailUrl', 'thumbnail')
content = content.replace('youtubeUrl', 'pdfUrl')

with open('app/src/main/java/com/example/ui/home/GlobalSearchScreen.kt', 'w') as f:
    f.write(content)
