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

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
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
                    enabled = !isLocked,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLocked,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (isLocked) return@Button
                            
                            if (email != "auracommunityact@gmail.com") {
                                errorMessage = "Email not valid."
                                return@Button
                            }
                            if (password != "@Shaan2011") {
                                errorMessage = "Incorrect password."
                                failedAttempts++
                                sharedPrefs.edit().putInt("failedAttempts", failedAttempts).apply()
                                
                                if (failedAttempts >= 5) {
                                    val time = System.currentTimeMillis()
                                    sharedPrefs.edit().putLong("lockTime", time).apply()
                                    lockTime = time
                                    isLocked = true
                                    errorMessage = "Too many failed attempts. Try again after 24 hours."
                                }
                                return@Button
                            }
                            
                            // Success
                            failedAttempts = 0
                            sharedPrefs.edit().putInt("failedAttempts", 0).putLong("lockTime", 0L).apply()
                            onLoginSuccess()
                        },
                        enabled = !isLocked
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}
