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
