package com.example.ui.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "profile_preferences")

class ProfileViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_PICTURE_URI = stringPreferencesKey("profile_picture_uri")
    }

    val profileName: StateFlow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PROFILE_NAME] ?: ""
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val profilePictureUri: StateFlow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PROFILE_PICTURE_URI] ?: ""
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun updateProfileName(name: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PROFILE_NAME] = name.trim()
            }
        }
    }

    fun updateProfilePictureUri(uri: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PROFILE_PICTURE_URI] = uri
            }
        }
    }
}
