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
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        private val _homeConfigUpdateTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
        val homeConfigUpdateTrigger = _homeConfigUpdateTrigger.asSharedFlow()

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

        suspend fun notifyHomeConfigChanged() {
            _homeConfigUpdateTrigger.emit(Unit)
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

    suspend fun searchUsers(query: String): List<User> {
        return try {
            client.postgrest["users"].select {
                filter {
                    or {
                        ilike("name", "%$query%")
                        ilike("email", "%$query%")
                        ilike("id", "%$query%")
                        ilike("mobileNumber", "%$query%")
                        ilike("studentId", "%$query%") // Also adding studentId as it's common for academic apps
                    }
                }
            }.decodeList<User>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createUserProfile(user: User) {
        try {
            client.postgrest["users"].insert(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateUserProfile(user: User) {
        try {
            client.postgrest["users"].update(user) {
                filter { eq("id", user.id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // Books
    suspend fun getBooks(): List<Book> {
        return try {
            client.postgrest["books"].select().decodeList<Book>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBook(bookId: String): Book? {
        return try {
            client.postgrest["books"].select {
                filter { eq("id", bookId) }
            }.decodeList<Book>().firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getBooksByClass(className: String): List<Book> {
        return try {
            client.postgrest["books"].select {
                filter { eq("className", className) }
            }.decodeList<Book>()
        } catch (e: Exception) {
            emptyList()
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
            val idValue: Any = bookId.toLongOrNull() ?: bookId
            client.postgrest["books"].delete {
                filter { eq("id", idValue) }
            }
            notifyBooksChanged()
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
            val idValue: Any = videoId.toLongOrNull() ?: videoId
            client.postgrest["videos"].delete {
                filter { eq("id", idValue) }
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
            e.printStackTrace()
            android.util.Log.e("AuraRepository", "Error fetching courses", e)
            throw e
        }
    }
    suspend fun getBanners(): List<Banner> {
        return try {
            client.postgrest["banners"].select {
                filter { eq("isEnabled", true) }
            }.decodeList<Banner>().sortedBy { it.order }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAnnouncements(): List<com.example.data.models.Announcement> {
        return try {
            client.postgrest["announcements"].select {
                filter { eq("isEnabled", true) }
            }.decodeList<com.example.data.models.Announcement>().sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getHomeSectionConfigs(): List<com.example.data.models.HomeSectionConfig> {
        return try {
            client.postgrest["home_sections"].select().decodeList<com.example.data.models.HomeSectionConfig>().sortedBy { it.order }
        } catch (e: Exception) {
            // Default sections if none exist in DB
            listOf(
                com.example.data.models.HomeSectionConfig("1", "books", "Featured Books", "📚", true, 1),
                com.example.data.models.HomeSectionConfig("2", "videos", "Featured Videos", "🎥", true, 2),
                com.example.data.models.HomeSectionConfig("3", "courses", "Popular Courses", "🎓", true, 3),
                com.example.data.models.HomeSectionConfig("4", "websites", "Educational Websites", "🌐", true, 4),
                com.example.data.models.HomeSectionConfig("5", "exams", "Practice & Exams", "📝", true, 5),
                com.example.data.models.HomeSectionConfig("6", "trending", "Trending Content", "🔥", true, 6),
                com.example.data.models.HomeSectionConfig("7", "recommended", "Recommended For You", "⭐", true, 7),
                com.example.data.models.HomeSectionConfig("8", "announcements", "Latest Announcements", "📢", true, 8)
            )
        }
    }

    suspend fun addBanner(banner: Banner) {
        try {
            val newBanner = if (banner.id.isEmpty()) banner.copy(id = UUID.randomUUID().toString()) else banner
            client.postgrest["banners"].insert(newBanner)
            notifyHomeConfigChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateBanner(banner: Banner) {
        try {
            client.postgrest["banners"].update(banner) {
                filter { eq("id", banner.id) }
            }
            notifyHomeConfigChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteBanner(bannerId: String) {
        try {
            val idValue: Any = bannerId.toLongOrNull() ?: bannerId
            client.postgrest["banners"].delete {
                filter { eq("id", idValue) }
            }
            notifyHomeConfigChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateHomeSectionConfig(config: com.example.data.models.HomeSectionConfig) {
        try {
            client.postgrest["home_sections"].update(config) {
                filter { eq("id", config.id) }
            }
            notifyHomeConfigChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
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
    
    suspend fun updateCourse(course: Course) {
        try {
            if (course.id.isNotEmpty()) {
                client.postgrest["courses"].update(course) {
                    filter { eq("id", course.id) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    suspend fun deleteCourse(courseId: String) {
        try {
            if (courseId.isNotEmpty()) {
                val idValue: Any = courseId.toLongOrNull() ?: courseId
                client.postgrest["courses"].delete {
                    filter { eq("id", idValue) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // Websites
    suspend fun getWebsites(): List<com.example.data.models.Website> {
        return try {
            client.postgrest["websites"].select().decodeList<com.example.data.models.Website>()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun addWebsite(website: com.example.data.models.Website) {
        try {
            val newWebsite = if (website.id.isEmpty()) website.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else website
            client.postgrest["websites"].insert(newWebsite)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    suspend fun updateWebsite(website: com.example.data.models.Website) {
        try {
            if (website.id.isNotEmpty()) {
                client.postgrest["websites"].update(website) {
                    filter { eq("id", website.id) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteWebsite(websiteId: String) {
        try {
            if (websiteId.isNotEmpty()) {
                val idValue: Any = websiteId.toLongOrNull() ?: websiteId
                client.postgrest["websites"].delete {
                    filter { eq("id", idValue) }
                }
            }
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

    suspend fun uploadCoverImage(imageBytes: ByteArray, fileName: String): String {
        return try {
            val bucket = client.storage["covers"]
            bucket.upload(fileName, imageBytes) { upsert = true }
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun uploadBookPdf(pdfBytes: ByteArray, fileName: String): String {
        return try {
            val bucket = client.storage["books"]
            bucket.upload(fileName, pdfBytes) { upsert = true }
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to "covers" bucket
            try {
                val bucket = client.storage["covers"]
                bucket.upload(fileName, pdfBytes) { upsert = true }
                bucket.publicUrl(fileName)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ""
            }
        }
    }

    fun subscribeToCourses(onChanged: () -> Unit) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val channel = client.realtime.channel("courses-updates")
                val changes = channel.postgresChangeFlow<PostgresAction>("public") {
                    table = "courses"
                }
                scope.launch {
                    changes.collect {
                        onChanged()
                    }
                }
                channel.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
