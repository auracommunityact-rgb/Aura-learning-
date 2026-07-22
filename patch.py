import re

with open('app/src/main/java/com/example/data/models/Models.kt', 'r') as f:
    content = f.read()

course_pattern = re.compile(r'@Serializable\ndata class Course\(.*?\n\)', re.DOTALL)
question_paper_str = """@Serializable
data class QuestionPaper(
    @Serializable(with = StringOrNumericSerializer::class)
    val id: String = "",
    val title: String = "",
    val className: String = "",
    val subject: String = "",
    val section: String = "",
    val board: String = "",
    val year: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val pdfUrl: String = "",
    val fileSize: String = "",
    val totalPages: Int = 0,
    val createdAt: Long = 0L
)"""

content = course_pattern.sub(question_paper_str, content)

with open('app/src/main/java/com/example/data/models/Models.kt', 'w') as f:
    f.write(content)

