package com.example.data.repository

import com.example.AppContext
import com.example.data.models.Banner
import com.example.data.models.Book
import com.example.data.models.Flashcard
import com.example.data.models.FlashcardDeck
import com.example.data.models.Note
import com.example.data.models.User
import com.example.data.models.Video
import com.example.data.models.VideoProgress
import com.example.data.models.BookProgress
import com.example.data.models.Course
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AuraRepository {
    private val client = SupabaseService.client
    private val prefs by lazy { AppContext.context.getSharedPreferences("guest_prefs", android.content.Context.MODE_PRIVATE) }

    companion object {
        // Shared update triggers across any instances of the repository
        private val _booksUpdateTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
        val booksUpdateTrigger = _booksUpdateTrigger.asSharedFlow()

        private val _videosUpdateTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
        val videosUpdateTrigger = _videosUpdateTrigger.asSharedFlow()

        private val _boardsUpdateTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
        val boardsUpdateTrigger = _boardsUpdateTrigger.asSharedFlow()

        private val _notificationsUpdateTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
        val notificationsUpdateTrigger = _notificationsUpdateTrigger.asSharedFlow()

        suspend fun notifyBooksChanged() {
            _booksUpdateTrigger.emit(Unit)
        }

        suspend fun notifyVideosChanged() {
            _videosUpdateTrigger.emit(Unit)
        }

        suspend fun notifyBoardsChanged() {
            _boardsUpdateTrigger.emit(Unit)
        }

        suspend fun notifyNotificationsChanged() {
            _notificationsUpdateTrigger.emit(Unit)
        }
    }

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
        val newBook = if (book.id.isEmpty()) book.copy(id = UUID.randomUUID().toString()) else book
        client.postgrest["books"].insert(newBook)
        notifyBooksChanged()
    }
    
    suspend fun updateBook(book: Book) {
        if (book.id.isNotEmpty()) {
            client.postgrest["books"].update(book) {
                filter { eq("id", book.id) }
            }
            notifyBooksChanged()
        }
    }
    
    suspend fun deleteBook(bookId: String) {
        if (bookId.isNotEmpty()) {
            client.postgrest["books"].delete {
                filter { eq("id", bookId) }
            }
            notifyBooksChanged()
        }
    }

    // Videos
    suspend fun getVideos(): List<Video> {
        val mathsLesson1 = Video(
            id = "maths_lesson_1",
            title = "Maths Class 10th - Part 1",
            description = "Maths Class 10th - Part 1",
            className = "10th",
            subject = "Maths",
            thumbnail = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/public/covers/mathematics-class-10-ncert.jpg",
            videoUrl = "https://youtu.be/SyZQP15qwaQ?si=1wKUi6W4YUHKds8h",
            youtubeVideoId = "SyZQP15qwaQ",
            chapter = "Maths Class 10th",
            partNumber = 1,
            teacher = "Aura Teacher",
            duration = "45:00",
            relatedBooks = listOf("maths_10th"),
            createdAt = System.currentTimeMillis()
        )
        val mathsLesson2 = Video(
            id = "maths_lesson_2",
            title = "Maths Class 10th - Part 2",
            description = "Maths Class 10th - Part 2",
            className = "10th",
            subject = "Maths",
            thumbnail = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/public/covers/mathematics-class-10-ncert.jpg",
            videoUrl = "https://youtu.be/YUscmq5Pr1Q?si=NkpgdUUXzIh62pTX",
            youtubeVideoId = "YUscmq5Pr1Q",
            chapter = "Maths Class 10th",
            partNumber = 2,
            teacher = "Aura Teacher",
            duration = "45:00",
            relatedBooks = listOf("maths_10th"),
            createdAt = System.currentTimeMillis()
        )
        return try {
            listOf(mathsLesson1, mathsLesson2) + client.postgrest["videos"].select().decodeList<Video>()
        } catch (e: Exception) {
            listOf(mathsLesson1, mathsLesson2)
        }
    }

    suspend fun getVideosByClass(className: String): List<Video> {
        val mathsLesson1 = Video(
            id = "maths_lesson_1",
            title = "Maths Class 10th - Part 1",
            description = "Maths Class 10th - Part 1",
            className = "10th",
            subject = "Maths",
            thumbnail = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/public/covers/mathematics-class-10-ncert.jpg",
            videoUrl = "https://youtu.be/SyZQP15qwaQ?si=1wKUi6W4YUHKds8h",
            youtubeVideoId = "SyZQP15qwaQ",
            chapter = "Maths Class 10th",
            partNumber = 1,
            teacher = "Aura Teacher",
            duration = "45:00",
            relatedBooks = listOf("maths_10th"),
            createdAt = System.currentTimeMillis()
        )
        val mathsLesson2 = Video(
            id = "maths_lesson_2",
            title = "Maths Class 10th - Part 2",
            description = "Maths Class 10th - Part 2",
            className = "10th",
            subject = "Maths",
            thumbnail = "https://qxoqflrqpwlythgqmjtq.supabase.co/storage/v1/object/public/covers/mathematics-class-10-ncert.jpg",
            videoUrl = "https://youtu.be/YUscmq5Pr1Q?si=NkpgdUUXzIh62pTX",
            youtubeVideoId = "YUscmq5Pr1Q",
            chapter = "Maths Class 10th",
            partNumber = 2,
            teacher = "Aura Teacher",
            duration = "45:00",
            relatedBooks = listOf("maths_10th"),
            createdAt = System.currentTimeMillis()
        )
        return try {
            val dbVideos = client.postgrest["videos"].select {
                filter { eq("className", className) }
            }.decodeList<Video>()
            if (className == "10th") listOf(mathsLesson1, mathsLesson2) + dbVideos else dbVideos
        } catch (e: Exception) {
            if (className == "10th") listOf(mathsLesson1, mathsLesson2) else emptyList()
        }
    }

    suspend fun getVideoById(videoId: String): Video? {
        val videos = getVideos()
        return videos.find { it.id == videoId }
    }

    suspend fun addVideo(video: Video) {
        val newVideo = if (video.id.isEmpty()) video.copy(id = UUID.randomUUID().toString()) else video
        client.postgrest["videos"].insert(newVideo)
        notifyVideosChanged()
    }
    
    suspend fun updateVideo(video: Video) {
        if (video.id.isNotEmpty()) {
            client.postgrest["videos"].update(video) {
                filter { eq("id", video.id) }
            }
            notifyVideosChanged()
        }
    }
    
    suspend fun deleteVideo(videoId: String) {
        if (videoId.isNotEmpty()) {
            client.postgrest["videos"].delete {
                filter { eq("id", videoId) }
            }
            notifyVideosChanged()
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
    // Courses
    suspend fun getCourses(): List<Course> {
        return try {
            client.postgrest["courses"].select().decodeList<Course>()
        } catch (e: Exception) {
            emptyList()
        }
    }
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

    suspend fun addCourse(course: Course) {
        try {
            val newCourse = if (course.id.isEmpty()) course.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else course
            client.postgrest["courses"].insert(newCourse)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
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

    // Progress Tracking
    suspend fun getVideoProgress(userId: String): List<VideoProgress> {
        return try {
            client.postgrest["video_progress"].select {
                filter { eq("userId", userId) }
            }.decodeList<VideoProgress>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateVideoProgress(progress: VideoProgress) {
        try {
            val existing = client.postgrest["video_progress"].select {
                filter {
                    eq("userId", progress.userId)
                    eq("videoId", progress.videoId)
                }
            }.decodeList<VideoProgress>().firstOrNull()

            if (existing != null) {
                client.postgrest["video_progress"].update(progress.copy(id = existing.id, lastWatchedAt = System.currentTimeMillis())) {
                    filter { eq("id", existing.id) }
                }
            } else {
                client.postgrest["video_progress"].insert(progress.copy(id = UUID.randomUUID().toString(), lastWatchedAt = System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getBookProgress(userId: String): List<BookProgress> {
        return try {
            client.postgrest["book_progress"].select {
                filter { eq("userId", userId) }
            }.decodeList<BookProgress>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateBookProgress(progress: BookProgress) {
        try {
            val existing = client.postgrest["book_progress"].select {
                filter {
                    eq("userId", progress.userId)
                    eq("bookId", progress.bookId)
                }
            }.decodeList<BookProgress>().firstOrNull()

            if (existing != null) {
                client.postgrest["book_progress"].update(progress.copy(id = existing.id, lastReadAt = System.currentTimeMillis())) {
                    filter { eq("id", existing.id) }
                }
            } else {
                client.postgrest["book_progress"].insert(progress.copy(id = UUID.randomUUID().toString(), lastReadAt = System.currentTimeMillis()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Exam Boards (Exam Results websites)
    suspend fun getExamBoards(): List<com.example.ui.profile.BoardResult> {
        return try {
            client.postgrest["exam_boards"].select().decodeList<com.example.ui.profile.BoardResult>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addExamBoard(board: com.example.ui.profile.BoardResult) {
        try {
            val newBoard = if (board.id.isEmpty()) board.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else board
            client.postgrest["exam_boards"].insert(newBoard)
            notifyBoardsChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateExamBoard(board: com.example.ui.profile.BoardResult) {
        try {
            if (board.id.isNotEmpty()) {
                client.postgrest["exam_boards"].update(board) {
                    filter { eq("id", board.id) }
                }
                notifyBoardsChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteExamBoard(boardId: String) {
        try {
            if (boardId.isNotEmpty()) {
                client.postgrest["exam_boards"].delete {
                    filter { eq("id", boardId) }
                }
                notifyBoardsChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // Notifications
    suspend fun addNotification(notification: com.example.data.repository.notifications.SupabaseNotification) {
        try {
            client.postgrest["notifications"].insert(notification)
            notifyNotificationsChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun uploadResultImage(imageBytes: ByteArray, fileName: String): String {
        return try {
            val bucket = client.storage["results"]
            bucket.upload(fileName, imageBytes) { upsert = true }
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
