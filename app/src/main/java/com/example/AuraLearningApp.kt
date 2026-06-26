package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.ViewModelFactory
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.books.BooksScreen
import com.example.ui.home.HomeScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.videos.VideosScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Videos : Screen("videos", "Videos", Icons.Filled.PlayCircle)
    object Books : Screen("books", "Books", Icons.Filled.MenuBook)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

val items = listOf(
    Screen.Home,
    Screen.Videos,
    Screen.Books,
    Screen.Profile
)

@Composable
fun AuraLearningApp() {
    val rootNavController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory)
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = rootNavController, startDestination = "main") {
        composable("login") { LoginScreen(rootNavController, authViewModel) }
        composable("register") { RegisterScreen(rootNavController, authViewModel) }
        composable("main") {
            MainScreen(authViewModel = authViewModel, rootNavController = rootNavController)
        }
    }
}

@Composable
fun MainScreen(authViewModel: AuthViewModel, rootNavController: androidx.navigation.NavController) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController, authViewModel, rootNavController) }
            composable(Screen.Videos.route) { VideosScreen(navController, authViewModel, rootNavController) }
            composable(Screen.Books.route) { BooksScreen(navController, authViewModel, rootNavController) }
            composable(Screen.Profile.route) { ProfileScreen(navController, authViewModel, rootNavController) }
        }
    }
}

