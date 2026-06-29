package com.example.data.repository

import com.example.AppContext
import com.example.data.models.Banner
import com.example.data.models.Book
import com.example.data.models.Flashcard
import com.example.data.models.FlashcardDeck
import com.example.data.models.Note
import com.example.data.models.User
import com.example.data.models.Video
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID

class AuraRepository {
    private val client = SupabaseService.client
    private val prefs by lazy { AppContext.context.getSharedPreferences("guest_prefs", android.content.Context.MODE_PRIVATE) }

    fun getGuestProfile(): User {
        val savedBooks = prefs.getStringSet("savedBooks", emptySet())?.toList() ?: emptyList()
        val savedVideos = prefs.getStringSet("savedVideos", emptySet())?.toList() ?: emptyList()
        return User(
            id = "guest_user",
            name = "Guest",
            role = "guest",
            savedBooks = savedBooks,
            savedVideos = savedVideos
        )
    }

    fun saveGuestProfile(user: User) {
        prefs.edit()
            .putStringSet("savedBooks", user.savedBooks.toSet())
            .putStringSet("savedVideos", user.savedVideos.toSet())
            .apply()
    }
    
    fun clearGuestProfile() {
        prefs.edit().clear().apply()
    }

    // Users
    suspend fun getUserProfile(uid: String): User? {
        return try {
            client.postgrest["users"].select {
                filter { eq("id", uid) }
            }.decodeSingle<User>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUserProfile(user: User) {
        try {
            client.postgrest["users"].insert(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Books
    suspend fun getBooks(): List<Book> {
        val kartikBook = Book(
            id = "kartik_10th",
            bookName = "Kartika",
            className = "10th",
            subject = "Hindi",
            coverImage = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/public/covers/31GDpJrSb3L._AC_UF1000,1000_QL80_.jpg",
            pdfUrl = "https://drive.google.com/file/d/1QXmCcR1XyEMjEdTWw1I4KsSNTTROgGMY/preview",
            createdAt = System.currentTimeMillis()
        )
        val mathsBook = Book(
            id = "maths_10th",
            bookName = "Maths",
            className = "10th",
            subject = "Mathematics",
            coverImage = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/public/covers/mathematics-class-10-ncert.jpg",
            pdfUrl = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/sign/Book/kemh1a1-combined.pdf?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV81NzhhNWYwOS03YzdjLTQ0MWMtODBmNy1jYjk2MTFiODQwYWMiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJCb29rL2tlbWgxYTEtY29tYmluZWQucGRmIiwic2NvcGUiOiJkb3dubG9hZCIsImlhdCI6MTc4Mjc2NzYzMiwiZXhwIjoxODE0MzAzNjMyfQ.Ci44hWuaNvjT7MVqlTOZ77pHliyIbewHQQpHnVTTKp0",
            createdAt = System.currentTimeMillis()
        )
        return try {
            val books = client.postgrest["books"].select().decodeList<Book>()
            listOf(kartikBook, mathsBook) + books
        } catch (e: Exception) {
            listOf(kartikBook, mathsBook)
        }
    }

    suspend fun getBooksByClass(className: String): List<Book> {
        val kartikBook = Book(
            id = "kartik_10th",
            bookName = "Kartika",
            className = "10th",
            subject = "Hindi",
            coverImage = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/public/covers/31GDpJrSb3L._AC_UF1000,1000_QL80_.jpg",
            pdfUrl = "https://drive.google.com/file/d/1QXmCcR1XyEMjEdTWw1I4KsSNTTROgGMY/preview",
            createdAt = System.currentTimeMillis()
        )
        val mathsBook = Book(
            id = "maths_10th",
            bookName = "Maths",
            className = "10th",
            subject = "Mathematics",
            coverImage = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/public/covers/mathematics-class-10-ncert.jpg",
            pdfUrl = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/sign/Book/kemh1a1-combined.pdf?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV81NzhhNWYwOS03YzdjLTQ0MWMtODBmNy1jYjk2MTFiODQwYWMiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJCb29rL2tlbWgxYTEtY29tYmluZWQucGRmIiwic2NvcGUiOiJkb3dubG9hZCIsImlhdCI6MTc4Mjc2NzYzMiwiZXhwIjoxODE0MzAzNjMyfQ.Ci44hWuaNvjT7MVqlTOZ77pHliyIbewHQQpHnVTTKp0",
            createdAt = System.currentTimeMillis()
        )
        return try {
            val books = client.postgrest["books"].select {
                filter { eq("className", className) }
            }.decodeList<Book>()
            if (className == "10th") listOf(kartikBook, mathsBook) + books else books
        } catch (e: Exception) {
            if (className == "10th") listOf(kartikBook, mathsBook) else emptyList()
        }
    }

    suspend fun addBook(book: Book) {
        try {
            val newBook = if (book.id.isEmpty()) book.copy(id = UUID.randomUUID().toString()) else book
            client.postgrest["books"].insert(newBook)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updateBook(book: Book) {
        if (book.id.isNotEmpty()) {
            try {
                client.postgrest["books"].update(book) {
                    filter { eq("id", book.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun deleteBook(bookId: String) {
        if (bookId.isNotEmpty()) {
            try {
                client.postgrest["books"].delete {
                    filter { eq("id", bookId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Videos
    suspend fun getVideos(): List<Video> {
        return try {
            client.postgrest["videos"].select().decodeList<Video>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVideosByClass(className: String): List<Video> {
        return try {
            client.postgrest["videos"].select {
                filter { eq("className", className) }
            }.decodeList<Video>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addVideo(video: Video) {
        try {
            val newVideo = if (video.id.isEmpty()) video.copy(id = UUID.randomUUID().toString()) else video
            client.postgrest["videos"].insert(newVideo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updateVideo(video: Video) {
        if (video.id.isNotEmpty()) {
            try {
                client.postgrest["videos"].update(video) {
                    filter { eq("id", video.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun deleteVideo(videoId: String) {
        if (videoId.isNotEmpty()) {
            try {
                client.postgrest["videos"].delete {
                    filter { eq("id", videoId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun getUsersCount(): Int {
        return try {
            // Very hacky but it works for now
            val users = client.postgrest["users"].select().decodeList<User>()
            users.size
        } catch (e: Exception) {
            0
        }
    }

    // Banners
    suspend fun getBanners(): List<Banner> {
        return try {
            client.postgrest["banners"].select().decodeList<Banner>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addBanner(banner: Banner) {
        try {
            val newBanner = if (banner.id.isEmpty()) banner.copy(id = UUID.randomUUID().toString()) else banner
            client.postgrest["banners"].insert(newBanner)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Notes
    suspend fun getNotesByUser(userId: String): List<Note> {
        return try {
            val notes = client.postgrest["notes"].select {
                filter { eq("userId", userId) }
            }.decodeList<Note>()
            notes.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addNote(note: Note) {
        try {
            val newNote = if (note.id.isEmpty()) note.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else note
            client.postgrest["notes"].insert(newNote)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteNote(noteId: String) {
        if (noteId.isNotEmpty()) {
            try {
                client.postgrest["notes"].delete {
                    filter { eq("id", noteId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Flashcards
    suspend fun getFlashcardDecksByUser(userId: String): List<FlashcardDeck> {
        return try {
            val decks = client.postgrest["flashcard_decks"].select {
                filter { eq("userId", userId) }
            }.decodeList<FlashcardDeck>()
            decks.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFlashcardDeck(deck: FlashcardDeck): String {
        return try {
            val newDeckId = UUID.randomUUID().toString()
            val newDeck = deck.copy(id = newDeckId, createdAt = System.currentTimeMillis())
            client.postgrest["flashcard_decks"].insert(newDeck)
            newDeckId
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun deleteFlashcardDeck(deckId: String) {
        if (deckId.isNotEmpty()) {
            try {
                client.postgrest["flashcard_decks"].delete {
                    filter { eq("id", deckId) }
                }
                client.postgrest["flashcards"].delete {
                    filter { eq("deckId", deckId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getFlashcardsByDeck(deckId: String): List<Flashcard> {
        return try {
            val cards = client.postgrest["flashcards"].select {
                filter { eq("deckId", deckId) }
            }.decodeList<Flashcard>()
            cards.sortedBy { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFlashcard(card: Flashcard) {
        try {
            val newCard = if (card.id.isEmpty()) card.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else card
            client.postgrest["flashcards"].insert(newCard)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteFlashcard(cardId: String) {
        if (cardId.isNotEmpty()) {
            try {
                client.postgrest["flashcards"].delete {
                    filter { eq("id", cardId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
