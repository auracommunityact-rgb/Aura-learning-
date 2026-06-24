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
