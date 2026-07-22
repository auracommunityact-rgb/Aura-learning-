#!/bin/bash
awk '
/import com.example.ui.quiz.QuizScreen/ {
    print "import com.example.ui.admin.AdminManageQuizzesScreen"
    print "import com.example.ui.admin.AdminAddEditQuizScreen"
}
/composable\("admin_manage_sections"\)/ {
    print "        composable(\"admin_manage_quizzes\") {"
    print "            AdminManageQuizzesScreen(rootNavController)"
    print "        }"
    print "        composable("
    print "            \"admin_add_edit_quiz/{quizId}\","
    print "            arguments = listOf(androidx.navigation.navArgument(\"quizId\") { type = androidx.navigation.NavType.StringType })"
    print "        ) { backStackEntry ->"
    print "            val quizId = backStackEntry.arguments?.getString(\"quizId\") ?: \"\""
    print "            AdminAddEditQuizScreen(rootNavController, quizId)"
    print "        }"
}
{ print }
' app/src/main/java/com/example/AuraLearningApp.kt > temp.kt && mv temp.kt app/src/main/java/com/example/AuraLearningApp.kt
