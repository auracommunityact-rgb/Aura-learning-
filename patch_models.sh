#!/bin/bash
sed -i 's/data class Course(/data class QuestionPaper(/' app/src/main/java/com/example/data/models/Models.kt
sed -i 's/val youtubeUrl: String = "",/val section: String = "",\n    val board: String = "",\n    val year: String = "",/' app/src/main/java/com/example/data/models/Models.kt
sed -i 's/val subject: String = "",/val className: String = "",\n    val subject: String = "",/' app/src/main/java/com/example/data/models/Models.kt
sed -i 's/val thumbnailUrl: String = "",/val thumbnail: String = "",/' app/src/main/java/com/example/data/models/Models.kt
sed -i 's/val contentFileUrl: String = "",/val pdfUrl: String = "",\n    val fileSize: String = "",\n    val totalPages: Int = 0,/' app/src/main/java/com/example/data/models/Models.kt
