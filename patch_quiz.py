with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

import re

content = re.sub(
    r'val newId = quiz\.id\s+val newQuiz = if \(quiz\.id\.isEmpty\(\)\) quiz\.copy\(createdAt = System\.currentTimeMillis\(\)\) else quiz\s+client\.postgrest\["quizzes"\]\.insert\(if \(quiz\.id\.isEmpty\(\)\) getJsonWithoutId\(newQuiz\) else newQuiz\)\s+newId',
    """val newQuiz = if (quiz.id.isEmpty()) quiz.copy(createdAt = System.currentTimeMillis()) else quiz
            val result = client.postgrest["quizzes"].insert(if (quiz.id.isEmpty()) getJsonWithoutId(newQuiz) else newQuiz) { select() }
            val inserted = result.decodeSingle<com.example.data.models.Quiz>()
            inserted.id""",
    content
)

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'w') as f:
    f.write(content)
