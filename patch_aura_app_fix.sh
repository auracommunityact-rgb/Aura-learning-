#!/bin/bash
sed -i 's/AdminManageQuizzesScreen(rootNavController)/com.example.ui.admin.AdminManageQuizzesScreen(rootNavController)/g' app/src/main/java/com/example/AuraLearningApp.kt
sed -i 's/AdminAddEditQuizScreen(rootNavController, quizId)/com.example.ui.admin.AdminAddEditQuizScreen(rootNavController, quizId)/g' app/src/main/java/com/example/AuraLearningApp.kt
