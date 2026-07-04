package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.User
import com.example.data.repository.AuraRepository
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuraRepository) : ViewModel() {
    private val client = SupabaseService.client
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        viewModelScope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> {
                        val user = status.session.user
                        if (user != null) {
                            val userProfile = repository.getUserProfile(user.id)
                            if (userProfile != null) {
                                _currentUser.value = userProfile
                                _authState.value = AuthState.Success
                            } else {
                                _authState.value = AuthState.Error("Profile not found")
                            }
                        }
                    }
                    is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated -> {
                        if (_currentUser.value?.id != "guest_user") {
                            _currentUser.value = repository.getGuestProfile()
                            _authState.value = AuthState.Idle
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }
                val user = client.auth.currentSessionOrNull()?.user
                if (user != null) {
                    val userProfile = repository.getUserProfile(user.id)
                    if (userProfile != null) {
                        _currentUser.value = userProfile
                        _authState.value = AuthState.Success
                    } else {
                        // User exists in auth but no profile, create one
                        val newUser = User(
                            id = user.id,
                            name = email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            email = email,
                            provider = "email",
                            createdAt = System.currentTimeMillis(),
                            role = if (email == "auracommunityact@gmail.com") "admin" else "user"
                        )
                        repository.createUserProfile(newUser)
                        _currentUser.value = newUser
                        _authState.value = AuthState.Success
                    }
                } else {
                    _authState.value = AuthState.Error("Login failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                }
                val user = client.auth.currentSessionOrNull()?.user
                if (user != null) {
                    val newUser = User(
                        id = user.id,
                        name = name.ifEmpty { email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } },
                        email = email,
                        provider = "email",
                        createdAt = System.currentTimeMillis(),
                        role = if (email == "auracommunityact@gmail.com") "admin" else "user"
                    )
                    repository.createUserProfile(newUser)
                    _currentUser.value = newUser
                    _authState.value = AuthState.Success
                } else {
                    // Check email confirmation requirement
                    _authState.value = AuthState.Error("Signup failed or email confirmation required.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                client.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }
                val user = client.auth.currentSessionOrNull()?.user
                if (user != null) {
                    val userProfile = repository.getUserProfile(user.id)
                    if (userProfile != null) {
                        _currentUser.value = userProfile
                        _authState.value = AuthState.Success
                    } else {
                        val newUser = User(
                            id = user.id,
                            name = user.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "User",
                            email = user.email ?: "",
                            provider = "google",
                            photoUrl = user.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "") ?: "",
                            createdAt = System.currentTimeMillis(),
                            role = if (user.email == "auracommunityact@gmail.com") "admin" else "user"
                        )
                        repository.createUserProfile(newUser)
                        _currentUser.value = newUser
                        _authState.value = AuthState.Success
                    }
                } else {
                     _authState.value = AuthState.Error("Google Auth failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Auth failed")
            }
        }
    }

    fun simulateGoogleSignIn(email: String) {
        // Just sign up with a dummy password since this is a simulation
        register(email.substringBefore("@"), email, "google_sim_password123")
    }

    val isAdmin: Boolean
        get() = _currentUser.value?.email == "auracommunityact@gmail.com"

    fun logout() {
        viewModelScope.launch {
            try {
                client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _currentUser.value = repository.getGuestProfile()
            _authState.value = AuthState.Idle
        }
    }
    
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            try {
                client.auth.resetPasswordForEmail(email)
                _authState.value = AuthState.Error("Password reset email sent.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send reset email.")
            }
        }
    }

    fun continueAsGuest() {
        _currentUser.value = repository.getGuestProfile()
        _authState.value = AuthState.Success
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun toggleSaveBook(bookId: String) {
        val user = _currentUser.value ?: return
        if (user.id == "guest_user") {
            val newSaved = if (user.savedBooks.contains(bookId)) {
                user.savedBooks - bookId
            } else {
                user.savedBooks + bookId
            }
            val newUser = user.copy(savedBooks = newSaved)
            _currentUser.value = newUser
            repository.saveGuestProfile(newUser)
        } else {
            val newSaved = if (user.savedBooks.contains(bookId)) {
                user.savedBooks - bookId
            } else {
                user.savedBooks + bookId
            }
            val newUser = user.copy(savedBooks = newSaved)
            _currentUser.value = newUser
            viewModelScope.launch {
                try {
                    client.postgrest["users"].update(newUser) {
                        filter { eq("id", newUser.id) }
                    }
                } catch(e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    fun toggleSaveVideo(videoId: String) {
        val user = _currentUser.value ?: return
        if (user.id == "guest_user") {
            val newSaved = if (user.savedVideos.contains(videoId)) {
                user.savedVideos - videoId
            } else {
                user.savedVideos + videoId
            }
            val newUser = user.copy(savedVideos = newSaved)
            _currentUser.value = newUser
            repository.saveGuestProfile(newUser)
        } else {
            val newSaved = if (user.savedVideos.contains(videoId)) {
                user.savedVideos - videoId
            } else {
                user.savedVideos + videoId
            }
            val newUser = user.copy(savedVideos = newSaved)
            _currentUser.value = newUser
            viewModelScope.launch {
                try {
                    client.postgrest["users"].update(newUser) {
                        filter { eq("id", newUser.id) }
                    }
                } catch(e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }
    fun updateSelectedGrade(grade: String) {
        val user = _currentUser.value ?: return
        if (user.id == "guest_user") {
            val newUser = user.copy(selectedGrade = grade)
            _currentUser.value = newUser
            repository.saveGuestProfile(newUser)
        } else {
            val newUser = user.copy(selectedGrade = grade)
            _currentUser.value = newUser
            viewModelScope.launch {
                try {
                    client.postgrest["users"].update(newUser) {
                        filter { eq("id", newUser.id) }
                    }
                } catch(e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
