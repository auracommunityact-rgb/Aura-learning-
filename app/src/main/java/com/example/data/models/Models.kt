package com.example.data.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val provider: String = "",
    val createdAt: Long = 0L,
    val role: String = "user",
    val savedBooks: List<String> = emptyList(),
    val savedVideos: List<String> = emptyList()
)

data class Book(
    val id: String = "",
    val bookName: String = "",
    val className: String = "",
    val subject: String = "",
    val coverImage: String = "",
    val pdfUrl: String = "",
    val createdAt: Long = 0L
)

data class Video(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val className: String = "",
    val subject: String = "",
    val thumbnail: String = "",
    val videoUrl: String = "",
    val createdAt: Long = 0L
)

data class Banner(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val link: String = "",
    val createdAt: Long = 0L
)
