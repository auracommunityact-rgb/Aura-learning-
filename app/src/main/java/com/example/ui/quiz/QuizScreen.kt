package com.example.ui.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavController,
    lessonId: String
) {
    val questions = remember(lessonId) {
        listOf(
            QuizQuestion(
                "1",
                "What is the key prerequisite for master-level learning?",
                listOf("Rote learning", "Spaced repetition and active recall", "Cramming before exams", "Passive reading"),
                1,
                "Scientific study shows that spaced repetition and active recall are the most effective study techniques."
            ),
            QuizQuestion(
                "2",
                "How does consistent goal-setting impact learning outcomes?",
                listOf("It has no direct impact", "It causes academic burnout", "It drives structured habits and consistent daily progress", "It is only useful for examinations"),
                2,
                "Consistent, measurable goal-setting creates psychological ownership and drives incremental progress."
            ),
            QuizQuestion(
                "3",
                "Which strategy is recommended when encountering complex topics?",
                listOf("Skimming and skipping hard details", "Breaking the topic down into smaller, structured concepts", "Memorizing whole chapters", "Avoiding practice questions"),
                1,
                "Deconstructing complex ideas into simpler constituent parts (First Principles) simplifies and accelerates deep understanding."
            )
        )
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lesson Quiz") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (quizCompleted) {
                QuizResultScreen(
                    score = score,
                    totalQuestions = questions.size,
                    onDone = { navController.popBackStack() }
                )
            } else if (currentQuestion != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress Indicator
                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Question ${currentQuestionIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Question Text
                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Options
                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrect = index == currentQuestion.correctOptionIndex
                        
                        val backgroundColor = when {
                            !showFeedback -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            showFeedback && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            showFeedback && isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                        
                        val borderColor = when {
                            !showFeedback -> if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            showFeedback && isCorrect -> Color(0xFF4CAF50)
                            showFeedback && isSelected && !isCorrect -> Color(0xFFF44336)
                            else -> Color.Transparent
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !showFeedback) {
                                    selectedOptionIndex = index
                                },
                            colors = CardDefaults.cardColors(containerColor = backgroundColor),
                            border = CardDefaults.outlinedCardBorder(true).copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(borderColor))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                if (showFeedback) {
                                    if (isCorrect) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = "Correct", tint = Color(0xFF4CAF50))
                                    } else if (isSelected) {
                                        Icon(Icons.Filled.Cancel, contentDescription = "Incorrect", tint = Color(0xFFF44336))
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AnimatedVisibility(visible = showFeedback) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Explanation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(currentQuestion.explanation, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            if (!showFeedback) {
                                showFeedback = true
                                if (selectedOptionIndex == currentQuestion.correctOptionIndex) {
                                    score++
                                }
                            } else {
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedOptionIndex = null
                                    showFeedback = false
                                } else {
                                    quizCompleted = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = selectedOptionIndex != null
                    ) {
                        Text(if (!showFeedback) "Check Answer" else if (currentQuestionIndex < questions.size - 1) "Next Question" else "Finish Quiz")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(score: Int, totalQuestions: Int, onDone: () -> Unit) {
    val percentage = (score.toFloat() / totalQuestions) * 100
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = if (percentage >= 50) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Quiz Completed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You scored $score out of $totalQuestions",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Text("Done")
        }
    }
}
