#!/bin/bash
sed -i 's/import com.example.ui.admin.AdminManageQuizzesScreen/import com.example.ui.admin.AdminManageQuizzesScreen\nimport com.example.ui.admin.AdminAddEditQuizScreen\nimport com.example.ui.quiz.QuizScreen/g' app/src/main/java/com/example/AuraLearningApp.kt
