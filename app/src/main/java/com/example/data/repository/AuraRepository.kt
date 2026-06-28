package com.example.data.repository

import com.example.data.models.Banner
import com.example.data.models.Book
import com.example.data.models.User
import com.example.data.models.Video
import com.example.AppContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuraRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
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

    // Auth
    fun getCurrentUser() = auth.currentUser

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUserProfile(user: User) {
        firestore.collection("users").document(user.id).set(user).await()
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
        return try {
            val snapshot = firestore.collection("books").get().await()
            listOf(kartikBook) + snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            listOf(kartikBook)
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
        return try {
            val snapshot = firestore.collection("books").whereEqualTo("className", className).get().await()
            val books = snapshot.toObjects(Book::class.java)
            if (className == "10th") listOf(kartikBook) + books else books
        } catch (e: Exception) {
            if (className == "10th") listOf(kartikBook) else emptyList()
        }
    }

    suspend fun addBook(book: Book) {
        val ref = firestore.collection("books").document()
        val newBook = book.copy(id = ref.id)
        ref.set(newBook).await()
    }
    
    suspend fun updateBook(book: Book) {
        if (book.id.isNotEmpty()) {
            firestore.collection("books").document(book.id).set(book).await()
        }
    }
    
    suspend fun deleteBook(bookId: String) {
        if (bookId.isNotEmpty()) {
            firestore.collection("books").document(bookId).delete().await()
        }
    }

    // Videos
    suspend fun getVideos(): List<Video> {
        return try {
            val snapshot = firestore.collection("videos").get().await()
            snapshot.toObjects(Video::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVideosByClass(className: String): List<Video> {
        return try {
            val snapshot = firestore.collection("videos").whereEqualTo("className", className).get().await()
            snapshot.toObjects(Video::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addVideo(video: Video) {
        val ref = firestore.collection("videos").document()
        val newVideo = video.copy(id = ref.id)
        ref.set(newVideo).await()
    }
    
    suspend fun updateVideo(video: Video) {
        if (video.id.isNotEmpty()) {
            firestore.collection("videos").document(video.id).set(video).await()
        }
    }
    
    suspend fun deleteVideo(videoId: String) {
        if (videoId.isNotEmpty()) {
            firestore.collection("videos").document(videoId).delete().await()
        }
    }
    
    suspend fun getUsersCount(): Int {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    // Banners
    suspend fun getBanners(): List<Banner> {
        return try {
            val snapshot = firestore.collection("banners").get().await()
            snapshot.toObjects(Banner::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addBanner(banner: Banner) {
        val ref = firestore.collection("banners").document()
        val newBanner = banner.copy(id = ref.id)
        ref.set(newBanner).await()
    }
}
