package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.AuraRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.books.BooksViewModel
import com.example.ui.home.HomeViewModel
import com.example.ui.profile.ProfileViewModel
import com.example.ui.videos.VideosViewModel

object ViewModelFactory : ViewModelProvider.Factory {
    private val repository = AuraRepository()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository) as T
            modelClass.isAssignableFrom(BooksViewModel::class.java) -> BooksViewModel(repository) as T
            modelClass.isAssignableFrom(VideosViewModel::class.java) -> VideosViewModel(repository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
