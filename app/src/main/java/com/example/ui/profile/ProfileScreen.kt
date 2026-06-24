package com.example.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.auth.AuthViewModel

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (currentUser != null) {
            Text("Profile", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Name: ${currentUser?.name}")
            Text("Email: ${currentUser?.email}")
            Text("Role: ${currentUser?.role}")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (currentUser?.role == "admin") {
                val context = androidx.compose.ui.platform.LocalContext.current
                Text("Admin Panel", style = MaterialTheme.typography.titleLarge)
                Divider()
                // Admin actions
                Button(onClick = { android.widget.Toast.makeText(context, "Upload book coming soon", android.widget.Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Upload Book")
                }
                Button(onClick = { android.widget.Toast.makeText(context, "Upload video coming soon", android.widget.Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Upload Video")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please login to view profile")
            }
        }
    }
}

