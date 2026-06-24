package com.example.data.repository

import com.example.data.models.Banner
import com.example.data.models.Book
import com.example.data.models.User
import com.example.data.models.Video
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuraRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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
        return try {
            val snapshot = firestore.collection("books").get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBooksByClass(className: String): List<Book> {
        return try {
            val snapshot = firestore.collection("books").whereEqualTo("className", className).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addBook(book: Book) {
        val ref = firestore.collection("books").document()
        val newBook = book.copy(id = ref.id)
        ref.set(newBook).await()
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
