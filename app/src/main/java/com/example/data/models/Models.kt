package com.example.data.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val provider: String = "Email", // "Email" or "Google"
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val role: String = "user", // "user" or "admin"
    val savedBooks: List<String> = emptyList(),
    val savedVideos: List<String> = emptyList()
)

data class Book(
    val id: String = "",
    val bookName: String = "",
    val coverImage: String = "",
    val pdfUrl: String = "",
    val className: String = "",
    val subject: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Video(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnail: String = "",
    val className: String = "",
    val subject: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Banner(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val linkUrl: String = ""
)
