package com.example.ui.admin

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.AuraRepository
import com.example.data.supabase.SupabaseService
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE) }
    
    var isLocked by remember { mutableStateOf(false) }
    var lockTime by remember { mutableStateOf(sharedPrefs.getLong("lockTime", 0L)) }
    var failedAttempts by remember { mutableStateOf(sharedPrefs.getInt("failedAttempts", 0)) }

    LaunchedEffect(lockTime) {
        if (lockTime > 0) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lockTime < 24 * 60 * 60 * 1000) {
                isLocked = true
                errorMessage = "Too many failed attempts. Try again after 24 hours."
            } else {
                isLocked = false
                failedAttempts = 0
                sharedPrefs.edit().putInt("failedAttempts", 0).putLong("lockTime", 0L).apply()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Admin Login", style = MaterialTheme.typography.titleLarge)
                
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    enabled = !isLocked && !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLocked && !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (isLocked || isSubmitting) return@Button
                            
                            if (email.trim() != "auracommunityact@gmail.com") {
                                errorMessage = "Email not valid."
                                return@Button
                            }
                            
                            isSubmitting = true
                            errorMessage = null
                            
                            coroutineScope.launch {
                                try {
                                    SupabaseService.client.auth.signInWith(Email) {
                                        this.email = email.trim()
                                        this.password = password
                                    }
                                    
                                    val user = SupabaseService.client.auth.currentSessionOrNull()?.user
                                    if (user == null) {
                                        errorMessage = "Sign in failed. Check credentials or confirm email."
                                        isSubmitting = false
                                        return@launch
                                    }
                                    
                                    val repo = AuraRepository()
                                    val profile = repo.getUserProfile(user.id)
                                    if (profile?.role == "admin" || user.email == "auracommunityact@gmail.com") {
                                        failedAttempts = 0
                                        sharedPrefs.edit().putInt("failedAttempts", 0).putLong("lockTime", 0L).apply()
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "You are not an admin."
                                        SupabaseService.client.auth.signOut()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("AdminLogin", "error", e)
                                    errorMessage = e.message ?: "Login failed"
                                    failedAttempts++
                                    sharedPrefs.edit().putInt("failedAttempts", failedAttempts).apply()
                                    
                                    if (failedAttempts >= 5) {
                                        val time = System.currentTimeMillis()
                                        sharedPrefs.edit().putLong("lockTime", time).apply()
                                        lockTime = time
                                        isLocked = true
                                        errorMessage = "Too many failed attempts. Try again after 24 hours."
                                    }
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isLocked && !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Login")
                        }
                    }
                }
            }
        }
    }
}
