package com.example.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val provider: String = "",
    val createdAt: Long = 0L,
    val role: String = "user",
    val savedBooks: List<String> = emptyList(),
    val savedVideos: List<String> = emptyList(),
    val selectedGrade: String = "All Grades"
)

@Serializable
data class Book(
    val id: String = "",
    val bookName: String = "",
    val className: String = "",
    val subject: String = "",
    val coverImage: String = "",
    val pdfUrl: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class Video(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val className: String = "",
    val subject: String = "",
    val thumbnail: String = "",
    val videoUrl: String = "", // Used as youtubeUrl
    val youtubeVideoId: String = "",
    val chapter: String = "",
    val partNumber: Int = 1,
    val teacher: String = "",
    val duration: String = "",
    val order: Int = 0,
    val relatedBooks: List<String> = emptyList(),
    val createdAt: Long = 0L
)

@Serializable
data class Banner(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val link: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class Note(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val associatedId: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class FlashcardDeck(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val className: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class Flashcard(
    val id: String = "",
    val deckId: String = "",
    val frontText: String = "",
    val backText: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class VideoProgress(
    val id: String = "",
    val userId: String = "",
    val videoId: String = "",
    val isWatched: Boolean = false,
    val lastWatchedAt: Long = 0L
)

@Serializable
data class BookProgress(
    val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val lastPage: Int = 0,
    val lastReadAt: Long = 0L
)
