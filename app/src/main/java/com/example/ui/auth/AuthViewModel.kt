package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.User
import com.example.data.repository.AuraRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val repository: AuraRepository) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        val fbUser = auth.currentUser
        if (fbUser != null) {
            viewModelScope.launch {
                val userProfile = repository.getUserProfile(fbUser.uid)
                if (userProfile != null) {
                    _currentUser.value = userProfile
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Profile not found")
                }
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                result.user?.let {
                    val userProfile = repository.getUserProfile(it.uid)
                    _currentUser.value = userProfile
                    _authState.value = AuthState.Success
                } ?: run {
                    _authState.value = AuthState.Error("Login failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { fbUser ->
                    val existingProfile = repository.getUserProfile(fbUser.uid)
                    val finalUser = if (existingProfile != null) {
                        existingProfile.copy(lastLoginAt = System.currentTimeMillis())
                    } else {
                        val role = if (fbUser.email == "auracommunityact@gmail.com") "admin" else "user"
                        User(
                            id = fbUser.uid,
                            name = fbUser.displayName ?: "Google User",
                            email = fbUser.email ?: "",
                            photoUrl = fbUser.photoUrl?.toString() ?: "",
                            provider = "Google",
                            createdAt = System.currentTimeMillis(),
                            lastLoginAt = System.currentTimeMillis(),
                            role = role
                        )
                    }
                    repository.createUserProfile(finalUser)
                    _currentUser.value = finalUser
                    _authState.value = AuthState.Success
                } ?: run {
                    _authState.value = AuthState.Error("Unable to sign in with Google. Please try again.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Authentication Failed")
            }
        }
    }

    fun simulateGoogleSignIn(testEmail: String = "google_student@aura.edu", testName: String = "Aura Student") {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val fakeUid = "google_sim_" + testEmail.hashCode()
                val existingProfile = repository.getUserProfile(fakeUid)
                val finalUser = if (existingProfile != null) {
                    existingProfile.copy(lastLoginAt = System.currentTimeMillis())
                } else {
                    val role = if (testEmail == "auracommunityact@gmail.com") "admin" else "user"
                    User(
                        id = fakeUid,
                        name = testName,
                        email = testEmail,
                        photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150&q=80",
                        provider = "Google",
                        createdAt = System.currentTimeMillis(),
                        lastLoginAt = System.currentTimeMillis(),
                        role = role
                    )
                }
                repository.createUserProfile(finalUser)
                _currentUser.value = finalUser
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to simulate login")
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                result.user?.let {
                    val role = if (email == "auracommunityact@gmail.com") "admin" else "user"
                    val newUser = User(id = it.uid, name = name, email = email, role = role)
                    repository.createUserProfile(newUser)
                    _currentUser.value = newUser
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            try {
                repository.createUserProfile(user)
                _currentUser.value = user
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun toggleSaveBook(bookId: String) {
        val user = _currentUser.value ?: return
        val newSavedBooks = if (user.savedBooks.contains(bookId)) {
            user.savedBooks - bookId
        } else {
            user.savedBooks + bookId
        }
        updateUserProfile(user.copy(savedBooks = newSavedBooks))
    }

    fun toggleSaveVideo(videoId: String) {
        val user = _currentUser.value ?: return
        val newSavedVideos = if (user.savedVideos.contains(videoId)) {
            user.savedVideos - videoId
        } else {
            user.savedVideos + videoId
        }
        updateUserProfile(user.copy(savedVideos = newSavedVideos))
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
