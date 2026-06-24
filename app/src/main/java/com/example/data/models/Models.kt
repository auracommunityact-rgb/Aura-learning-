package com.example.data.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user" // "user" or "admin"
)

data class Book(
    val id: String = "",
    val bookName: String = "",
    val coverImage: String = "",
    val pdfUrl: String = "",
    val className: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Video(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnail: String = "",
    val className: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Banner(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val linkUrl: String = ""
)
