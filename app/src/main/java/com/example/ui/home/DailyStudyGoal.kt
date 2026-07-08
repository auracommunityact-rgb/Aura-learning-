package com.example.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StudyGoalManager(context: Context) {
    private val prefs = context.getSharedPreferences("study_goals", Context.MODE_PRIVATE)

    private val _dailyGoalMinutes = MutableStateFlow(prefs.getInt("daily_goal_minutes", 60))
    val dailyGoalMinutes: StateFlow<Int> = _dailyGoalMinutes.asStateFlow()

    private val _studiedMinutes = MutableStateFlow(prefs.getInt("studied_minutes", 0))
    val studiedMinutes: StateFlow<Int> = _studiedMinutes.asStateFlow()

    fun setDailyGoal(minutes: Int) {
        prefs.edit().putInt("daily_goal_minutes", minutes).apply()
        _dailyGoalMinutes.value = minutes
    }

    fun addStudyTime(minutes: Int) {
        val newTime = _studiedMinutes.value + minutes
        prefs.edit().putInt("studied_minutes", newTime).apply()
        _studiedMinutes.value = newTime
    }
    
    fun resetStudyTime() {
        prefs.edit().putInt("studied_minutes", 0).apply()
        _studiedMinutes.value = 0
    }
}

@Composable
fun DailyStudyGoalCard() {
    val context = LocalContext.current
    val goalManager = remember { StudyGoalManager(context) }
    
    val goalMinutes by goalManager.dailyGoalMinutes.collectAsState()
    val studiedMinutes by goalManager.studiedMinutes.collectAsState()
    
    var showGoalDialog by remember { mutableStateOf(false) }
    
    val progress = if (goalMinutes > 0) {
        (studiedMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Study Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$studiedMinutes / $goalMinutes mins",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }
                    
                    FilledTonalButton(
                        onClick = { goalManager.addStudyTime(10) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Time", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("10m", fontSize = 12.sp)
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    if (showGoalDialog) {
        var newGoalInput by remember { mutableStateOf(goalMinutes.toString()) }
        
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Daily Goal") },
            text = {
                OutlinedTextField(
                    value = newGoalInput,
                    onValueChange = { newGoalInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Goal (minutes)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newGoal = newGoalInput.toIntOrNull()
                        if (newGoal != null && newGoal > 0) {
                            goalManager.setDailyGoal(newGoal)
                        }
                        showGoalDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
