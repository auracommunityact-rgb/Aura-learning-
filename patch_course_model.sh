#!/bin/bash
cat << 'INNER_EOF' > tmp_course_model.kt
@Serializable
data class Course(
    @Serializable(with = StringOrNumericSerializer::class)
    val id: String = "",
    val subject: String? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val youtubeUrl: String? = null,
    val contentFileUrl: String? = null,
    val createdAt: Long? = 0L
)
INNER_EOF

# we can use sed to replace it
awk '
/data class Course/ {
    p = 1
    system("cat tmp_course_model.kt")
    next
}
p == 1 && /^\)/ {
    p = 0
    next
}
p == 0 { print }
' app/src/main/java/com/example/data/models/Models.kt > temp.kt
mv temp.kt app/src/main/java/com/example/data/models/Models.kt
