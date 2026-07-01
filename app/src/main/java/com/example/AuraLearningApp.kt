package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.ui.theme.ThemeViewModel


import com.example.ui.admin.AdminLoginDialog
import com.example.ui.admin.AdminDashboardScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Study : Screen("study", "Study", Icons.Filled.Edit)
    object Videos : Screen("videos", "Videos", Icons.Filled.PlayCircle)
    object Books : Screen("books", "Books", Icons.AutoMirrored.Filled.MenuBook)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

val items = listOf(
    Screen.Home,
    Screen.Study,
    Screen.Videos,
    Screen.Books,
    Screen.Profile
)

@Composable
fun AuraLearningApp(themeViewModel: ThemeViewModel? = null) {
    val rootNavController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory)
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = rootNavController, startDestination = "splash") {
        composable("splash") { com.example.ui.splash.SplashScreen(rootNavController) }
        composable("login") { LoginScreen(rootNavController, authViewModel) }
        composable("register") { RegisterScreen(rootNavController, authViewModel) }
        composable("admin_dashboard") { AdminDashboardScreen(rootNavController) }
        composable("exam_results") { com.example.ui.profile.ExamResultScreen(rootNavController, rootNavController) }
        composable(
            "exam_webview?url={url}&title={title}",
            arguments = listOf(
                androidx.navigation.navArgument("url") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: "Result"
            com.example.ui.profile.ResultWebViewScreen(navController = rootNavController, url = url, title = title)
        }
        composable(
            "pdf_viewer?url={url}",
            arguments = listOf(androidx.navigation.navArgument("url") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val bookId = url.hashCode().toString()
            com.example.ui.books.PdfViewerScreen(navController = rootNavController, pdfUrl = url, bookId = bookId)
        }
        composable(
            "video_player/{videoId}",
            arguments = listOf(androidx.navigation.navArgument("videoId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            com.example.ui.videos.VideoPlayerScreen(navController = rootNavController, videoId = videoId)
        }
        composable(
            "flashcards/{deckId}",
            arguments = listOf(androidx.navigation.navArgument("deckId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            com.example.ui.study.FlashcardsScreen(navController = rootNavController, deckId = deckId)
        }
        composable("study_planner") { com.example.ui.study.planner.StudyPlannerScreen(rootNavController) }
        composable("create_schedule") { com.example.ui.study.planner.CreateScheduleScreen(rootNavController) }
        composable("notes_translate") { com.example.ui.study.NotesTranslateScreen(rootNavController) }
        composable("ai_chat") { com.example.ui.chat.PuterChatScreen(rootNavController) }
        composable(
            "quiz/{lessonId}",
            arguments = listOf(androidx.navigation.navArgument("lessonId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            com.example.ui.quiz.QuizScreen(navController = rootNavController, lessonId = lessonId)
        }
        composable(
            "tool_viewer/{toolId}?title={title}",
            arguments = listOf(
                androidx.navigation.navArgument("toolId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType; defaultValue = "Study Tool" }
            )
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getString("toolId") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: "Study Tool"
            com.example.ui.study.ToolViewerScreen(navController = rootNavController, toolId = toolId, title = title)
        }
        composable("main") {
            MainScreen(authViewModel = authViewModel, rootNavController = rootNavController, themeViewModel = themeViewModel)
        }
    }
}

@Composable
fun MainScreen(authViewModel: AuthViewModel, rootNavController: androidx.navigation.NavController, themeViewModel: ThemeViewModel? = null) {
    val navController = rememberNavController()

    
    var profileTaps by remember { mutableStateOf(0) }
    var lastProfileTapTime by remember { mutableStateOf(0L) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            onDismiss = { showAdminLoginDialog = false },
            onLoginSuccess = {
                showAdminLoginDialog = false
                rootNavController.navigate("admin_dashboard")
            }
        )
    }

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
                            if (screen == Screen.Profile) {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastProfileTapTime < 2000) {
                                    profileTaps++
                                    if (profileTaps == 3) {
                                        showAdminLoginDialog = true
                                        profileTaps = 0
                                    }
                                } else {
                                    profileTaps = 1
                                }
                                lastProfileTapTime = currentTime
                            } else {
                                profileTaps = 0
                            }
                            
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
            composable("global_search") { com.example.ui.home.GlobalSearchScreen(navController, rootNavController) }
            composable(Screen.Study.route) { com.example.ui.study.StudyScreen(navController, authViewModel, rootNavController) }
            composable(Screen.Videos.route) { VideosScreen(navController, authViewModel, rootNavController) }
            composable(Screen.Books.route) { BooksScreen(navController, authViewModel, rootNavController) }
            composable(Screen.Profile.route) { ProfileScreen(navController, authViewModel, rootNavController, themeViewModel) }
        }
    }
}

