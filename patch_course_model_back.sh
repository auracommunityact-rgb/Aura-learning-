awk '
/data class Course/ {
    p = 1
    print "@Serializable"
    print "data class Course("
    print "    @Serializable(with = StringOrNumericSerializer::class)"
    print "    val id: String = \"\","
    print "    val subject: String = \"\","
    print "    val title: String = \"\","
    print "    val description: String = \"\","
    print "    val thumbnailUrl: String = \"\","
    print "    val youtubeUrl: String = \"\","
    print "    val contentFileUrl: String = \"\","
    print "    val createdAt: Long = 0L"
    print ")"
    next
}
p == 1 && /^\)/ {
    p = 0
    next
}
p == 0 { print }
' app/src/main/java/com/example/data/models/Models.kt > temp.kt
mv temp.kt app/src/main/java/com/example/data/models/Models.kt
perl -0777 -pi -e 's/\@Serializable\n\@Serializable/\@Serializable/g' app/src/main/java/com/example/data/models/Models.kt
