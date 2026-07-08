package com.example.ui.chat

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.ViewModelFactory
import com.example.ui.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Model representation of a Chat Message
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isQuiz: Boolean = false,
    val quizQuestion: String? = null,
    val quizOptions: List<String>? = null,
    val correctAnswerIndex: Int? = null,
    val selectedOptionIndex: Int? = null,
    val answered: Boolean = false,
    val quizFeedback: String? = null
)

@Composable
fun PomodoroTimerWidget() {
    var isExpanded by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isBreak by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (isRunning && timeLeft == 0) {
            isRunning = false
            if (isBreak) {
                timeLeft = 25 * 60
                isBreak = false
            } else {
                timeLeft = 5 * 60
                isBreak = true
            }
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header row with toggle option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { isExpanded = !isExpanded },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Pomodoro Timer",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBreak) "Break Session" else "Focus Study Timer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isExpanded) {
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isBreak) "Take a well-deserved rest" else "Stay focused on your lessons",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier.background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(
                            onClick = {
                                isRunning = false
                                timeLeft = if (isBreak) 5 * 60 else 25 * 60
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        TextButton(
                            onClick = {
                                isRunning = false
                                isBreak = !isBreak
                                timeLeft = if (isBreak) 5 * 60 else 25 * 60
                            }
                        ) {
                            Text(
                                text = if (isBreak) "Focus Mode" else "Break Mode",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// Typing Indicator
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    val dot1Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.2f at 0
                1f at 300
                0.2f at 600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1"
    )
    val dot2Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.2f at 150
                1f at 450
                0.2f at 750
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2"
    )
    val dot3Alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.2f at 300
                1f at 600
                0.2f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Aura AI is thinking", 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = dot1Alpha), CircleShape))
        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = dot2Alpha), CircleShape))
        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = dot3Alpha), CircleShape))
    }
}

// Simple Parser for basic Markdown like bold (**text**) and code (`code`)
fun parseMarkdown(text: String, primaryColor: Color): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var index = 0
        while (index < text.length) {
            val boldStart = text.indexOf("**", index)
            val codeStart = text.indexOf("`", index)
            
            val nextSpecial = when {
                boldStart != -1 && codeStart != -1 -> minOf(boldStart, codeStart)
                boldStart != -1 -> boldStart
                else -> codeStart
            }
            
            if (nextSpecial == -1) {
                append(text.substring(index))
                break
            }
            
            // Append raw text before special token
            append(text.substring(index, nextSpecial))
            
            if (nextSpecial == boldStart) {
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append(text.substring(boldStart + 2, boldEnd))
                    }
                    index = boldEnd + 2
                } else {
                    append("**")
                    index = boldStart + 2
                }
            } else {
                val codeEnd = text.indexOf("`", codeStart + 1)
                if (codeEnd != -1) {
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = primaryColor.copy(alpha = 0.12f),
                            color = primaryColor
                        )
                    ) {
                        append(text.substring(codeStart + 1, codeEnd))
                    }
                    index = codeEnd + 1
                } else {
                    append("`")
                    index = codeStart + 1
                }
            }
        }
    }
}

// Conversational Intelligence Engine (Fully Offline Context-Aware Tutor)
fun getAuraResponse(
    input: String,
    userName: String,
    booksList: List<com.example.data.models.Book>,
    coursesList: List<com.example.data.models.Course>,
    examsList: List<com.example.data.local.ExamDateSheetEntity>,
    sessionsList: List<com.example.data.local.StudySession>,
    calculatorHistory: List<com.example.data.local.CalculatorHistoryEntity>
): ChatMessage {
    val lowerInput = input.lowercase().trim()

    // 1. Detect educational grade/class level (1 to 12)
    var detectedGrade = 0
    val class12Keywords = listOf("class 12", "class xii", "12th", "grade 12", "twelfth", "twelveth")
    val class11Keywords = listOf("class 11", "class xi", "11th", "grade 11", "eleventh")
    val class10Keywords = listOf("class 10", "class x", "10th", "grade 10", "class ten", "tenth")
    val class9Keywords = listOf("class 9", "class ix", "9th", "grade 9", "ninth")
    val class8Keywords = listOf("class 8", "class viii", "8th", "grade 8", "eighth")
    val class7Keywords = listOf("class 7", "class vii", "7th", "grade 7", "seventh")
    val class6Keywords = listOf("class 6", "class vi", "6th", "grade 6", "sixth")
    val class5Keywords = listOf("class 5", "class v", "5th", "grade 5", "fifth")
    val class4Keywords = listOf("class 4", "class iv", "4th", "grade 4", "fourth")
    val class3Keywords = listOf("class 3", "class iii", "3rd", "grade 3", "third")
    val class2Keywords = listOf("class 2", "class ii", "2nd", "grade 2", "second")
    val class1Keywords = listOf("class 1", "class i", "1st", "grade 1", "first")

    if (class12Keywords.any { lowerInput.contains(it) }) detectedGrade = 12
    else if (class11Keywords.any { lowerInput.contains(it) }) detectedGrade = 11
    else if (class10Keywords.any { lowerInput.contains(it) }) detectedGrade = 10
    else if (class9Keywords.any { lowerInput.contains(it) }) detectedGrade = 9
    else if (class8Keywords.any { lowerInput.contains(it) }) detectedGrade = 8
    else if (class7Keywords.any { lowerInput.contains(it) }) detectedGrade = 7
    else if (class6Keywords.any { lowerInput.contains(it) }) detectedGrade = 6
    else if (class5Keywords.any { lowerInput.contains(it) }) detectedGrade = 5
    else if (class4Keywords.any { lowerInput.contains(it) }) detectedGrade = 4
    else if (class3Keywords.any { lowerInput.contains(it) }) detectedGrade = 3
    else if (class2Keywords.any { lowerInput.contains(it) }) detectedGrade = 2
    else if (class1Keywords.any { lowerInput.contains(it) }) detectedGrade = 1

    // 2. Detect subject
    var detectedSubject = ""
    if (lowerInput.contains("trig") || lowerInput.contains("calculus") || lowerInput.contains("differentiation") || lowerInput.contains("integration") || lowerInput.contains("quadratic") || lowerInput.contains("algebra") || lowerInput.contains("math") || lowerInput.contains("ganit") || lowerInput.contains("geometry") || lowerInput.contains("fraction") || lowerInput.contains("addition") || lowerInput.contains("calculation") || lowerInput.contains("hisab")) {
        detectedSubject = "Mathematics"
    } else if (lowerInput.contains("physics") || lowerInput.contains("force") || lowerInput.contains("electricity") || lowerInput.contains("gravity") || lowerInput.contains("gravitation") || lowerInput.contains("light") || lowerInput.contains("lens") || lowerInput.contains("mirror")) {
        detectedSubject = "Physics"
    } else if (lowerInput.contains("chemistry") || lowerInput.contains("acid") || lowerInput.contains("base") || lowerInput.contains("salt") || lowerInput.contains("chemical") || lowerInput.contains("reaction") || lowerInput.contains("bonding") || lowerInput.contains("mole concept")) {
        detectedSubject = "Chemistry"
    } else if (lowerInput.contains("photosynthesis") || lowerInput.contains("cell") || lowerInput.contains("biology") || lowerInput.contains("organelle") || lowerInput.contains("digest") || lowerInput.contains("plant") || lowerInput.contains("respiration") || lowerInput.contains("human")) {
        detectedSubject = "Biology"
    } else if (lowerInput.contains("science") || lowerInput.contains("vigyan")) {
        detectedSubject = "Science"
    } else if (lowerInput.contains("history") || lowerInput.contains("itihas") || lowerInput.contains("revolution") || lowerInput.contains("struggle") || lowerInput.contains("gandhi") || lowerInput.contains("maurya") || lowerInput.contains("mughal")) {
        detectedSubject = "History"
    } else if (lowerInput.contains("geography") || lowerInput.contains("map") || lowerInput.contains("soil") || lowerInput.contains("river") || lowerInput.contains("earth") || lowerInput.contains("mountain") || lowerInput.contains("plate tectonics")) {
        detectedSubject = "Geography"
    } else if (lowerInput.contains("civics") || lowerInput.contains("constitution") || lowerInput.contains("parliament") || lowerInput.contains("rights") || lowerInput.contains("democracy")) {
        detectedSubject = "Civics"
    } else if (lowerInput.contains("english") || lowerInput.contains("grammar") || lowerInput.contains("verb") || lowerInput.contains("noun") || lowerInput.contains("tense") || lowerInput.contains("voice")) {
        detectedSubject = "English"
    } else if (lowerInput.contains("hindi") || lowerInput.contains("sangya") || lowerInput.contains("sandhi") || lowerInput.contains("samas") || lowerInput.contains("alankar")) {
        detectedSubject = "Hindi"
    }

    // Direct routing for tool queries
    val isToolQuery = lowerInput.contains("book") || lowerInput.contains("kitab") || lowerInput.contains("video") || lowerInput.contains("course") || lowerInput.contains("exam") || lowerInput.contains("date") || lowerInput.contains("sheet") || lowerInput.contains("planner") || lowerInput.contains("schedule") || lowerInput.contains("calculator")

    if (isToolQuery) {
        return when {
            lowerInput.contains("book") || lowerInput.contains("kitab") || lowerInput.contains("padhne") || lowerInput.contains("syllabus") || lowerInput.contains("padhna") || lowerInput.contains("pustak") -> {
                if (booksList.isNotEmpty()) {
                    val sb = StringBuilder()
                    sb.append("### 📚 Aapki Books & Study Materials\n\n")
                    sb.append("Main aapki library ki sabhi standard standard NCERT/CBSE books access kar sakta hoon! Yeh rahi aapki offline standard books:\n\n")
                    sb.append("| Book Name | Subject | Class |\n")
                    sb.append("|---|---|---|\n")
                    booksList.forEach { book ->
                        sb.append("| **${book.bookName}** | ${book.subject} | ${book.className} |\n")
                    }
                    sb.append("\n**Aura AI Tutor Tip:** Aap inme se kisi bhi book ko directly **PDF Screen** me select karke offline open kar sakte hain aur notes prepare kar sakte hain! 📖")
                    ChatMessage(role = "assistant", content = sb.toString())
                } else {
                    ChatMessage(
                        role = "assistant",
                        content = "### 📚 Books Library\n\nAbhi app ki database me koi books downloaded nahi hain. Aap books load karne ke liye main panel par **Books Library** check karein!\n\n**Tip:** Books load hone ke baad main unke topics directly clear kar paunga."
                    )
                }
            }

            lowerInput.contains("video") || lowerInput.contains("course") || lowerInput.contains("lecture") || lowerInput.contains("videos") || lowerInput.contains("class") -> {
                if (coursesList.isNotEmpty()) {
                    val sb = StringBuilder()
                    sb.append("### 🎥 Aapke Available Video Lectures\n\n")
                    sb.append("Aapke offline high-quality video sessions directly study board par load ho chuke hain:\n\n")
                    coursesList.forEach { course ->
                        sb.append("- 🌟 **${course.title}**\n")
                        sb.append("  - **Subject:** ${course.subject}\n")
                        sb.append("  - **About:** *${course.description}*\n\n")
                    }
                    sb.append("**Tip:** In classes ko play karne ke liye main dashboard par video tutorial list use karein!")
                    ChatMessage(role = "assistant", content = sb.toString())
                } else {
                    ChatMessage(
                        role = "assistant",
                        content = "### 🎥 Video Courses & Lectures\n\nAbhi database me video lectures loaded nahi hain. Aap app screen par relevant subject videos load karke dekh sakte hain!"
                    )
                }
            }

            lowerInput.contains("exam") || lowerInput.contains("date") || lowerInput.contains("sheet") || lowerInput.contains("countdown") || lowerInput.contains("pariksha") || lowerInput.contains("time table") -> {
                if (examsList.isNotEmpty()) {
                    val sb = StringBuilder()
                    sb.append("### 📅 Exam Date Sheets & Countdowns\n\n")
                    sb.append("Maine aapke scheduled board/school exam count sheets ko retrieve kiya hai. Dil laga kar taiyari kijiye!\n\n")
                    sb.append("| Subject | Date & Day | Time | Grade |\n")
                    sb.append("|---|---|---|---|\n")
                    examsList.forEach { exam ->
                        sb.append("| **${exam.subject}** | ${exam.examDate} (${exam.examDay}) | ${exam.examTime} | Class ${exam.grade} |\n")
                    }
                    sb.append("\n**Aura Motivation:** Har subject ko balance time dein aur dynamic notes ka revision karte rahein. Aap zaroor top karenge! 💪")
                    ChatMessage(role = "assistant", content = sb.toString())
                } else {
                    ChatMessage(
                        role = "assistant",
                        content = "### 📅 Exam Countdowns\n\nAapke countdown tool me abhi tak koi exams scheduled nahi hain! \n\n**Guidance:** App ke home screen par **Exams Countdown** card par click karke apne subject exams add karein taaki main unka reminder deta rahoon."
                    )
                }
            }

            lowerInput.contains("schedule") || lowerInput.contains("plan") || lowerInput.contains("planner") || lowerInput.contains("routine") || lowerInput.contains("session") || lowerInput.contains("study session") -> {
                if (sessionsList.isNotEmpty()) {
                    val sb = StringBuilder()
                    sb.append("### 🗓️ Active Study Planner Schedules\n\n")
                    sb.append("Aapke scheduled study slots mere pass completely updated hain:\n\n")
                    sessionsList.take(6).forEach { session ->
                        val isComp = session.completedStatus == "COMPLETED"
                        val statusEmoji = if (isComp) "✅ Completed" else "⏳ Pending"
                        val dateStr = try {
                            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(session.dateMillis))
                        } catch (e: Exception) {
                            "Scheduled Date"
                        }
                        sb.append("- 🎯 Subject: **${session.subject}** (Topic: *${session.topic}*)\n")
                        sb.append("  - **Timing:** $dateStr | **Duration:** ${session.durationMins} mins\n")
                        sb.append("  - **Status:** $statusEmoji\n\n")
                    }
                    val hasPending = sessionsList.any { it.completedStatus != "COMPLETED" }
                    if (hasPending) {
                        sb.append("**Mentor Advice:** Aapke kuch sessions abhi bhi pending hain. Unhe study slot me jald se jald pura karein!")
                    } else {
                        sb.append("**Mentor Advice:** Bahut badhiya! Sabhi tasks completed hain. Aise hi consistent bane rahein!")
                    }
                    ChatMessage(role = "assistant", content = sb.toString())
                } else {
                    ChatMessage(
                        role = "assistant",
                        content = "### 🗓️ Study Planner\n\nAapke database study planner me abhi koi session target set nahi hai.\n\n**Action:** Home page par **Study Planner** par click karein aur apna personal study time table set karein!"
                    )
                }
            }

            else -> {
                if (calculatorHistory.isNotEmpty()) {
                    val sb = StringBuilder()
                    sb.append("### 🧮 Recent Calculations & Equation Answers\n\n")
                    sb.append("Aapne haal hi me jo math calculation seekhi thi, uska history record niche hai:\n\n")
                    calculatorHistory.take(5).forEach { calc ->
                        sb.append("- Calculated: `${calc.expression}` ➔ Result: **`${calc.answer}`**\n")
                    }
                    sb.append("\n**Math Solver Advice:** Complex equations ko solve karne ke liye aap quadratic formulas use kar sakte hain! Jaise: *'solve x² - 5x + 6'*.")
                    ChatMessage(role = "assistant", content = sb.toString())
                } else {
                    ChatMessage(
                        role = "assistant",
                        content = "### 🧮 Calculator History\n\nAbhi tak calculation records empty hain. \n\n**Tip:** Aap direct math solve karne ke liye calculator section use karein ya fir quadratic equations offline mere se poochhein!"
                    )
                }
            }
        }
    }

    // Check specific subjects and grade combinations
    return when (detectedSubject) {
        "Mathematics" -> {
            if (detectedGrade >= 11) {
                ChatMessage(
                    role = "assistant",
                    content = "### 📐 Class $detectedGrade Mathematics: Calculus (Differentiation)\n\nCalculus is a highly powerful tool used to calculate the rates of change or slope of curves.\n\n**1. Basic Derivative Formulas:**\n- `d/dx (xⁿ) = n·xⁿ⁻¹`\n- `d/dx (sin x) = cos x`\n- `d/dx (eˣ) = eˣ`\n\n**2. Solved Example (Step-by-Step):**\nFind the derivative of `y = 3x² + 5x`.\n- **Step 1:** Apply sum rule: `dy/dx = d/dx (3x²) + d/dx (5x)`\n- **Step 2:** Bring constants out: `dy/dx = 3 · d/dx (x²) + 5 · d/dx (x)`\n- **Step 3:** Apply power rule: `dy/dx = 3(2x) + 5(1) = 6x + 5`!\n\n**Let's test your understanding with a quick MCQ quiz below!**",
                    isQuiz = true,
                    quizQuestion = "What is the derivative of x³ + 4x with respect to x?",
                    quizOptions = listOf("A) 3x² + 4", "B) 3x + 4", "C) x² + 4", "D) 3x³ + 4"),
                    correctAnswerIndex = 0
                )
            } else if (detectedGrade in 9..10) {
                ChatMessage(
                    role = "assistant",
                    content = "### 📝 Class $detectedGrade Mathematics: Quadratic Equations & Formulas\n\nA quadratic equation is in the standard form: `ax² + bx + c = 0` (where `a ≠ 0`).\n\n**1. The Quadratic Formula:**\n`x = [-b ± √(b² - 4ac)] / 2a`\n- Here, `D = b² - 4ac` is the **Discriminant** which decides the nature of roots:\n  - If `D > 0`, roots are real and distinct.\n  - If `D = 0`, roots are real and equal.\n  - If `D < 0`, roots are imaginary.\n\n**2. Solved Example (Factorization):**\nSolve: `x² - 5x + 6 = 0`\n- **Step 1:** Find two numbers that multiply to `6` and add to `-5`. They are `-2` and `-3`.\n- **Step 2:** Split middle term: `x² - 2x - 3x + 6 = 0`\n- **Step 3:** Factor common terms: `x(x - 2) - 3(x - 2) = 0` ➔ `(x-2)(x-3) = 0`.\n- **Roots:** `x = 2` and `x = 3`!\n\n**Let's practice with a quick MCQ!**",
                    isQuiz = true,
                    quizQuestion = "What is the discriminant (b² - 4ac) of the equation x² - 4x + 4 = 0?",
                    quizOptions = listOf("A) 16", "B) 8", "C) 0", "D) -8"),
                    correctAnswerIndex = 2
                )
            } else if (detectedGrade in 6..8) {
                ChatMessage(
                    role = "assistant",
                    content = "### 🔢 Class $detectedGrade Mathematics: Linear Equations\n\nLinear equations help us find unknown values easily by balancing both sides of the equation!\n\n**1. Real-World Analogy:**\nThink of an equation as a weighing balance (tarazu). Whatever operation you perform on the left side, you must do on the right side to keep it perfectly balanced.\n\n**2. Step-by-Step Solving:**\nSolve for `x`: `3x + 7 = 22`\n- **Step 1 (Subtract 7 from both sides):** \n  `3x + 7 - 7 = 22 - 7` ➔ `3x = 15`\n- **Step 2 (Divide both sides by 3):** \n  `3x / 3 = 15 / 3` ➔ **`x = 5`**!\n\n**Try this simple check MCQ!**",
                    isQuiz = true,
                    quizQuestion = "Solve for y: 2y - 4 = 10. What is the value of y?",
                    quizOptions = listOf("A) y = 5", "B) y = 7", "C) y = 6", "D) y = 14"),
                    correctAnswerIndex = 1
                )
            } else {
                // Lower grade math
                ChatMessage(
                    role = "assistant",
                    content = "### 🍕 Class ${if (detectedGrade > 0) detectedGrade else "1-5"} Mathematics: Fractions Made Fun!\n\nWhat is a fraction? A fraction represents a part of a whole. \n\n**1. Pizza Analogy:**\nImagine a delicious chocolate pizza cut into **4 equal slices**:\n- If you eat **1 slice**, you have eaten `1/4` (one-fourth) of the pizza.\n- If you eat **2 slices**, you have eaten `2/4` or `1/2` (half) of the pizza!\n\n**2. Terminology:**\n- **Numerator (Top Number):** How many parts we have.\n- **Denominator (Bottom Number):** Total equal parts.\n\n**Test your pizza math below!**",
                    isQuiz = true,
                    quizQuestion = "If a cake is divided into 8 equal parts and you eat 3 parts, what fraction of the cake is left?",
                    quizOptions = listOf("A) 3/8", "B) 5/8", "C) 8/8", "D) 1/2"),
                    correctAnswerIndex = 1
                )
            }
        }

        "Physics" -> {
            if (detectedGrade >= 11) {
                ChatMessage(
                    role = "assistant",
                    content = "### ⚡ Class $detectedGrade Physics: Electrostatics & Coulomb's Law\n\nElectrostatics is the study of electromagnetic forces between static (non-moving) electrical charges.\n\n**1. Coulomb's Law Formula:**\n`F = k · (|q₁ · q₂|) / r²`\n- `F` is the Electrostatic Force (Newtons).\n- `q₁` and `q₂` are magnitudes of charges (Coulombs).\n- `r` is the distance between the center of charges (meters).\n- `k` is Coulomb's Constant (`8.987 × 10⁹ N·m²/C²`).\n\n**2. Core Principle:**\nLike charges repel each other, whereas opposite charges attract each other. The force is inversely proportional to the square of distance (Inverse Square Law).\n\n**Answer this high-level Physics question!**",
                    isQuiz = true,
                    quizQuestion = "If the distance between two electric charges is doubled, what happens to the electrostatic force (F) between them?",
                    quizOptions = listOf("A) Force is doubled", "B) Force is halved", "C) Force becomes 4 times", "D) Force becomes 1/4 of original"),
                    correctAnswerIndex = 3
                )
            } else if (detectedGrade in 9..10) {
                ChatMessage(
                    role = "assistant",
                    content = "### 🌌 Class $detectedGrade Physics: Gravitation & Universal Law\n\nWhy does an apple fall from a tree? Sir Isaac Newton explained this using the Universal Law of Gravitation!\n\n**1. Universal Formula:**\n`F = G · (m₁ · m₂) / r²`\n- `m₁` and `m₂` are the masses of the two objects.\n- `r` is the distance between them.\n- `G` is the Universal Gravitational Constant: `6.674 × 10⁻¹¹ N·m²/kg²`.\n\n**2. Acceleration due to Gravity (g):**\nOn Earth's surface, the acceleration experienced by falling bodies is `g = G · M_earth / R_earth² ≈ 9.8 m/s²`.\n\n**Let's test this law offline!**",
                    isQuiz = true,
                    quizQuestion = "Does the gravitational constant (G) change when we go to the Moon or space?",
                    quizOptions = listOf("A) Yes, it increases", "B) Yes, it decreases to zero", "C) No, it remains constant everywhere", "D) It depends on local mass"),
                    correctAnswerIndex = 2
                )
            } else {
                ChatMessage(
                    role = "assistant",
                    content = "### 🎾 Class ${if (detectedGrade > 0) detectedGrade else "1-8"} Physics: Force & Friction\n\nWhy does a rolling football stop automatically on the grass? It's because of a invisible stopping force called **Friction**!\n\n**1. What is Friction?**\nFriction is a resistance force that acts in the opposite direction of moving bodies. It occurs due to microscopic roughness of surfaces rubbing together.\n\n**2. Advantages & Disadvantages:**\n- **Good:** Helps us walk without slipping, allows brakes to stop cars.\n- **Bad:** Produces unwanted heat in machines, wears out shoe soles.\n\n**Give the correct answer below!**",
                    isQuiz = true,
                    quizQuestion = "Which of the following surfaces offers the LEAST friction to a moving object?",
                    quizOptions = listOf("A) Sandpaper", "B) Wet concrete road", "C) Smooth sheet of ice", "D) Dry grass field"),
                    correctAnswerIndex = 2
                )
            }
        }

        "Chemistry" -> {
            if (detectedGrade >= 11) {
                ChatMessage(
                    role = "assistant",
                    content = "### 🧪 Class $detectedGrade Chemistry: Mole Concept & Stoichiometry\n\nThe Mole is the SI unit of measurement for amount of substance. It bridges the microscopic atomic mass and macroscopic gram mass.\n\n**1. Avogadro's Number:**\n`1 Mole = 6.022 × 10²³ particles (atoms, molecules, or ions)`\n\n**2. Formulas:**\n- `Number of Moles (n) = Given Mass (m) / Molar Mass (M)`\n- `Volume of 1 Mole of any gas at STP = 22.4 Liters`\n\n**Example calculation:**\nHow many moles are in 36 grams of Water (`H₂O`, Molar Mass = 18 g/mol)?\n`n = 36g / 18g/mol = 2 moles`!\n\n**Answer the mole quiz question below:**",
                    isQuiz = true,
                    quizQuestion = "What is the mass of exactly 1 mole of Carbon-12 atoms?",
                    quizOptions = listOf("A) 1 gram", "B) 6.022 × 10²³ grams", "C) 12 grams", "D) 12 AMU"),
                    correctAnswerIndex = 2
                )
            } else if (detectedGrade in 9..10) {
                ChatMessage(
                    role = "assistant",
                    content = "### 🍋 Class $detectedGrade Chemistry: Acids, Bases, and Salts\n\nIn chemical classification, compounds are grouped into acids, bases, or salts based on hydrogen ions concentration!\n\n**1. Key Definitions:**\n- **Acids:** Sour in taste, release `H⁺` ions in water, turn blue litmus paper RED (pH < 7).\n- **Bases:** Bitter taste, soapy feel, release `OH⁻` ions, turn red litmus BLUE (pH > 7).\n- **Salts:** Formed by **Neutralization reactions** between acids and bases.\n\n**2. Neutralization Reaction Equation:**\n`Acid + Base ➔ Salt + Water`\n`HCl + NaOH ➔ NaCl + H₂O`\n\n**Check your acid-base knowledge offline!**",
                    isQuiz = true,
                    quizQuestion = "What is the pH value of a completely neutral water solution?",
                    quizOptions = listOf("A) pH = 1", "B) pH = 14", "C) pH = 7", "D) pH = 0"),
                    correctAnswerIndex = 2
                )
            } else {
                ChatMessage(
                    role = "assistant",
                    content = "### 🎈 Class ${if (detectedGrade > 0) detectedGrade else "1-8"} Chemistry: Physical vs Chemical Changes\n\nMatter around us undergoes two main types of modifications: Physical and Chemical!\n\n**1. Physical Change:**\nNo new substance is formed, and it is usually temporary and reversible.\n- *Example:* Ice melting into liquid water, tearing a paper sheet.\n\n**2. Chemical Change:**\nA brand new substance is manufactured with new chemical properties, and it is permanent/irreversible.\n- *Example:* Burning wood (ash is formed), rusting of iron, milk turning into curd.\n\n**Let's test this chemistry logic!**",
                    isQuiz = true,
                    quizQuestion = "Which of the following processes is a chemical change?",
                    quizOptions = listOf("A) Boiling of water", "B) Melting of wax", "C) Digestion of food in stomach", "D) Cutting of a tree branch"),
                    correctAnswerIndex = 2
                )
            }
        }

        "Biology" -> {
            if (detectedGrade >= 11) {
                ChatMessage(
                    role = "assistant",
                    content = "### 🧬 Class $detectedGrade Biology: Cell Division & Mitosis\n\nCell division is the biological mechanism where cells multiply, supporting tissue growth, repair, and reproduction.\n\n**1. Mitosis (Equational Division):**\nOccurs in somatic cells. One parent cell splits into **two identical diploid daughter cells**.\n\n**2. Main Stages of Mitosis:**\n- **Prophase:** Chromatin condenses, spindle fibers begin to form.\n- **Metaphase:** Chromosomes align perfectly at the cell equatorial plate.\n- **Anaphase:** Sister chromatids pull apart to opposite poles.\n- **Telophase:** Nuclear membranes reform around separate chromosome sets.\n\n**Test your cell biology knowledge!**",
                    isQuiz = true,
                    quizQuestion = "During which stage of mitosis do sister chromatids align along the center of the cell?",
                    quizOptions = listOf("A) Prophase", "B) Metaphase", "C) Anaphase", "D) Telophase"),
                    correctAnswerIndex = 1
                )
            } else if (detectedGrade in 9..10) {
                ChatMessage(
                    role = "assistant",
                    content = "### 🌿 Class $detectedGrade Biology: Photosynthesis Process\n\nHow do plants make food? They don't have kitchens, so they use **Photosynthesis**!\n\n**1. The Biochemical Equation:**\n`6CO₂ + 6H₂O + Solar Energy (Chlorophyll) ➔ C₆H₁₂O₆ (Glucose) + 6O₂`\n\n**2. Essential Raw Materials:**\n- **Sunlight:** Trapped by green chlorophyll pigments in leaves.\n- **Carbon Dioxide (CO₂):** Taken from air via tiny microscopic pores called **Stomata**.\n- **Water (H₂O):** Absorbed by roots from the soil.\n\n**Solve this interactive science puzzle below!**",
                    isQuiz = true,
                    quizQuestion = "Which gas is released into the atmosphere as a byproduct during photosynthesis?",
                    quizOptions = listOf("A) Carbon Dioxide", "B) Nitrogen", "C) Oxygen", "D) Helium"),
                    correctAnswerIndex = 2
                )
            } else {
                ChatMessage(
                    role = "assistant",
                    content = "### 🦁 Class ${if (detectedGrade > 0) detectedGrade else "1-8"} Biology: Food Chains & Ecosystems\n\nEvery living organism needs energy to live. A **Food Chain** shows who eats whom in nature to transfer this energy!\n\n**1. The Three Main Players:**\n- **Producers (Green Plants):** Make their own food using sunlight.\n- **Consumers (Animals):** Eat plants (Herbivores) or other animals (Carnivores).\n- **Decomposers (Fungi & Bacteria):** Clean up dead plants and animal waste.\n\n**2. Simple Example:**\n`Grass (Producer) ➔ Grasshopper ➔ Frog ➔ Snake ➔ Eagle`\n\n**Test your wild ecology IQ!**",
                    isQuiz = true,
                    quizQuestion = "Which of the following is always at the starting level of any natural food chain?",
                    quizOptions = listOf("A) Lion", "B) Green Plant", "C) Eagle", "D) Earthworm"),
                    correctAnswerIndex = 1
                )
            }
        }

        "History" -> {
            if (detectedGrade >= 9) {
                ChatMessage(
                    role = "assistant",
                    content = "### ⚜️ Class $detectedGrade History: The French Revolution (1789)\n\nThe French Revolution is a landmark historical chapter that gave the world the values of **Liberty, Equality, and Fraternity**.\n\n**1. Core Causes:**\n- **Social Inequality:** French society was split into **Three Estates**. Only the poorest Third Estate paid all royal taxes.\n- **Financial Bankruptcy:** King Louis XVI's wars drained the treasury.\n- **Subsistence Crisis:** Bread prices skyrocketed due to bad harvests.\n\n**2. Major Milestones:**\n- **July 14, 1789:** Storming of the **Bastille prison fort**, signaling the collapse of absolute monarchy!\n\n**Test your European History below!**",
                    isQuiz = true,
                    quizQuestion = "Who was the King of France when the French Revolution broke out in 1789?",
                    quizOptions = listOf("A) Louis XIV", "B) Louis XVI", "C) Napoleon Bonaparte", "D) Louis XVIII"),
                    correctAnswerIndex = 1
                )
            } else {
                ChatMessage(
                    role = "assistant",
                    content = "### 🏺 Class ${if (detectedGrade > 0) detectedGrade else "1-8"} History: Indus Valley Civilization\n\nDid you know that over 4,500 years ago, India had highly modern planned cities? This was the **Indus Valley (Harappan) Civilization**!\n\n**1. Amazing Town Planning:**\n- Cities like **Harappa** and **Mohenjo-daro** had straight grid-pattern roads intersecting at 90 degrees.\n- Homes had private bathrooms and brick-paved lanes with covered drains!\n- They had a massive public bath house called the **Great Bath**.\n\n**Solve this archaeology mystery quiz!**",
                    isQuiz = true,
                    quizQuestion = "Near which river bank did the ancient Harappan civilization develop?",
                    quizOptions = listOf("A) Ganga", "B) Yamuna", "C) Indus River", "D) Narmada"),
                    correctAnswerIndex = 2
                )
            }
        }

        "Geography" -> {
            if (detectedGrade >= 9) {
                ChatMessage(
                    role = "assistant",
                    content = "### 🌍 Class $detectedGrade Geography: Plate Tectonics & Continental Drift\n\nEarth's crust is not a single continuous shell; it is divided into massive moving puzzle slabs called **Tectonic Plates**!\n\n**1. Types of Plate Boundaries:**\n- **Convergent Boundary:** Plates collide, forming tall fold mountains (e.g., the mighty Himalayas).\n- **Divergent Boundary:** Plates pull apart, creating rift valleys.\n- **Transform Boundary:** Plates slide horizontally past each other, causing heavy earthquakes.\n\n**Answer this dynamic physical geography question!**",
                    isQuiz = true,
                    quizQuestion = "Which type of plate boundary is primarily responsible for the formation of the Himalayan Mountains?",
                    quizOptions = listOf("A) Divergent Boundary", "B) Transform Boundary", "C) Convergent Boundary", "D) Subduction Zone"),
                    correctAnswerIndex = 2
                )
            } else {
                ChatMessage(
                    role = "assistant",
                    content = "### ☀️ Class ${if (detectedGrade > 0) detectedGrade else "1-8"} Geography: Our Solar System\n\nWe live on Earth, which is a tiny blue planet revolving around our massive energy star, the **Sun**!\n\n**1. Key Solar System Facts:**\n- There are **8 planets** divided into Inner Rocky (Mercury, Venus, Earth, Mars) and Outer Gas Giants (Jupiter, Saturn, Uranus, Neptune).\n- **Venus** is the hottest planet, and **Jupiter** is the largest planet.\n- **Earth's Rotation (24 hrs):** Causes Day and Night.\n- **Earth's Revolution (365 days):** Causes Seasons.\n\n**Test your space science skills!**",
                    isQuiz = true,
                    quizQuestion = "Which planet in our solar system is famously known as the 'Red Planet'?",
                    quizOptions = listOf("A) Venus", "B) Mars", "C) Saturn", "D) Mercury"),
                    correctAnswerIndex = 1
                )
            }
        }

        "Civics" -> {
            ChatMessage(
                role = "assistant",
                content = "### 📜 Class ${if (detectedGrade > 0) detectedGrade else "6-12"} Civics: The Indian Constitution\n\nA constitution represents the supreme set of rules and laws that a country follows to govern its citizens!\n\n**1. Core Pillars of Democracy:**\n- **Legislature:** Makes laws (Parliament).\n- **Executive:** Implements laws (Prime Minister & Cabinet).\n- **Judiciary:** Protects laws and rights (Supreme Court).\n\n**2. Dr. B.R. Ambedkar:** Known as the Father of the Indian Constitution, which is the longest written national constitution globally!\n\n**Try this civic check MCQ!**",
                isQuiz = true,
                quizQuestion = "On which day did the Constitution of India officially come into force, celebrated as Republic Day?",
                quizOptions = listOf("A) 15th August 1947", "B) 26th November 1949", "C) 26th January 1950", "D) 2nd October 1869"),
                correctAnswerIndex = 2
            )
        }

        "English" -> {
            if (detectedGrade >= 9) {
                ChatMessage(
                    role = "assistant",
                    content = "### ✍️ Class $detectedGrade English: Active vs Passive Voice\n\nVoice describes whether the subject performs the action (Active) or receives the action (Passive)!\n\n**1. Core Conversion Formulas:**\n- **Active:** `Subject + Verb + Object`\n  - *Example:* \"Rohan writes a beautiful letter.\"\n- **Passive:** `Object + Auxiliary Verb + Verb (3rd Form) + by + Subject`\n  - *Example:* \"A beautiful letter **is written by** Rohan.\"\n\n**Let's test your grammar rules!**",
                    isQuiz = true,
                    quizQuestion = "Convert to Passive: 'The chef prepared a delicious dinner.'",
                    quizOptions = listOf(
                        "A) A delicious dinner is prepared by the chef.",
                        "B) A delicious dinner was prepared by the chef.",
                        "C) A delicious dinner had prepared by the chef.",
                        "D) A delicious dinner will be prepared by the chef."
                    ),
                    correctAnswerIndex = 1
                )
            } else {
                ChatMessage(
                    role = "assistant",
                    content = "### 📚 Class ${if (detectedGrade > 0) detectedGrade else "1-8"} English: Nouns & Pronouns\n\nLet's clear your basic grammar building blocks easily with examples!\n\n**1. What is a Noun?**\nA Noun is a naming word representing a **Person, Place, Animal, or Thing**.\n- *Example:* Rohan (Person), Delhi (Place), Cat (Animal), Pen (Thing).\n\n**2. What is a Pronoun?**\nA Pronoun replaces a noun to avoid repeating the same word.\n- *Example:* \"Rohan is a boy. **He** studies in Class 6.\" (Here, \"He\" is the pronoun).\n\n**Complete this simple grammar quiz!**",
                    isQuiz = true,
                    quizQuestion = "Identify the PRONOUN in the sentence: 'Aura said that she is reading a book.'",
                    quizOptions = listOf("A) Aura", "B) she", "C) reading", "D) book"),
                    correctAnswerIndex = 1
                )
            }
        }

        "Hindi" -> {
            if (detectedGrade >= 8) {
                ChatMessage(
                    role = "assistant",
                    content = "### 📖 Class $detectedGrade हिंदी व्याकरण: समास (Samas) सीखें\n\nसमास का अर्थ होता है - 'संक्षेप' या छोटा करना। दो या दो से अधिक शब्दों के मेल से बने नए शब्द को समास कहते हैं।\n\n**समास के मुख्य भेद:**\n1. **अव्ययीभाव समास:** पहला पद प्रधान और अव्यय होता है। (जैसे: यथाशक्ति = शक्ति के अनुसार)\n2. **तत्पुरुष समास:** उत्तर पद प्रधान होता है और कारक चिह्न का लोप होता है। (जैसे: राजपुत्र = राजा का पुत्र)\n3. **द्विगु समास:** पहला पद संख्यावाचक होता है। (जैसे: चौराहा = चार राहों का समूह)\n4. **द्वंद्व समास:** दोनों पद प्रधान होते हैं। (जैसे: माता-पिता = माता और पिता)\n5. **बहुव्रीहि समास:** कोई तीसरा अर्थ प्रधान होता है। (जैसे: लंबोदर = लंबा है उदर जिनका अर्थात् गणेश जी)\n\n**इस व्याकरण प्रश्न का उत्तर दें:**",
                    isQuiz = true,
                    quizQuestion = "'यथासंभव' शब्द में कौन-सा समास है?",
                    quizOptions = listOf("A) तत्पुरुष समास", "B) द्वंद्व समास", "C) अव्ययीभाव समास", "D) बहुव्रीहि समास"),
                    correctAnswerIndex = 2
                )
            } else {
                ChatMessage(
                    role = "assistant",
                    content = "### 🍎 Class ${if (detectedGrade > 0) detectedGrade else "1-7"} हिंदी व्याकरण: संज्ञा और सर्वनाम\n\nव्याकरण को आसान उदाहरणों के साथ समझें:\n\n**1. संज्ञा (Noun):**\nकिसी व्यक्ति, वस्तु, स्थान, या भाव के नाम को संज्ञा कहते हैं।\n- *उदाहरण:* अमन (व्यक्ति), सेब (वस्तु), दिल्ली (स्थान), ख़ुशी (भाव)।\n\n**2. सर्वनाम (Pronoun):**\nसंज्ञा के स्थान पर प्रयोग होने वाले शब्दों को सर्वनाम कहते हैं ताकि बार-बार नाम न दोहराना पड़े।\n- *उदाहरण:* वह, तुम, मैं, हम, उसका।\n- *वाक्य:* \"अमन अच्छा लड़का है। **वह** रोज़ स्कूल जाता है।\" (यहाँ **वह** सर्वनाम है)।\n\n**इस व्याकरण प्रश्न का उत्तर दें:**",
                    isQuiz = true,
                    quizQuestion = "दिए गए शब्दों में 'स्थानवाचक संज्ञा' कौन-सी है?",
                    quizOptions = listOf("A) रोहन", "B) जयपुर", "C) किताब", "D) मिठास"),
                    correctAnswerIndex = 1
                )
            }
        }

        else -> {
            // Check for general science if "science" or "vigyan" matched general
            if (lowerInput.contains("science") || lowerInput.contains("vigyan")) {
                ChatMessage(
                    role = "assistant",
                    content = "### 🔬 Class ${if (detectedGrade > 0) detectedGrade else "1-12"} General Science: The Scientific Method\n\nScience is the systematic study of the physical and natural world through observation and experimentation.\n\n**1. Core Scientific Fields:**\n- **Physics:** Study of matter, energy, and motion.\n- **Chemistry:** Study of elements, substances, and reactions.\n- **Biology:** Study of living organisms and life cycles.\n\n**Let's test your general scientific logic below!**",
                    isQuiz = true,
                    quizQuestion = "Which cellular organelle is responsible for cellular respiration, generating ATP, and is commonly known as the 'powerhouse of the cell'?",
                    quizOptions = listOf("A) Chloroplast", "B) Mitochondria", "C) Ribosome", "D) Lysosome"),
                    correctAnswerIndex = 1
                )
            } else {
                // Default Greetings and Conversational fallbacks
                val isGreeting = lowerInput.contains("hi") || lowerInput.contains("hello") || lowerInput.contains("hey") || lowerInput.contains("aura") || lowerInput.contains("kaise ho") || lowerInput.contains("help")
                
                if (isGreeting) {
                    ChatMessage(
                        role = "assistant",
                        content = "### 👋 Hello, $userName! Main hoon **Aura AI Tutor**, aapka dynamic personal companion! 🌟\n\nMain aapki 1st standard se lekar 12th standard tak ke **sabhi educational subjects** (Mathematics, Physics, Chemistry, Biology, History, Geography, Civics, English, Hindi) ko puri tarah **offline** clear kar sakta hoon! Mujhe kisi bhi external API key ya internet connection ki zaroorat nahi hai. \n\n**Aap mere se is tarah ke saval pooch sakte hain:**\n\n- 📐 \"Explain trigonometry of Class 10\"\n- ⚡ \"Explain Coulomb's Law of Class 12 Physics\"\n- 🌿 \"What is photosynthesis?\"\n- ⚜️ \"French Revolution ke baare me batao (Class 9 History)\"\n- 📖 \"हिंदी में समास के भेद समझाओ\"\n- 🍕 \"Fractions for Class 5 Math\"\n\n**Aap kis topic ya class ke baare me seekhna chahte hain? Type kijiye ya suggestion check karein!**"
                    )
                } else {
                    // Smart offline educational guide fallback
                    val gradeText = if (detectedGrade > 0) "Class $detectedGrade" else "1st to 12th Standard"
                    val subjectText = if (detectedSubject.isNotEmpty()) detectedSubject else "any school subject"
                    
                    ChatMessage(
                        role = "assistant",
                        content = "### 💡 Dynamic Offline Study Guide ($gradeText)\n\nMaine aapka question (*\"$input\"*) read kiya hai! Main bina kisi internet connection ya server-side Gemini API key ke directly local standards aur curriculum maps se response prepare karta hoon. \n\nAap mere se $gradeText ke organic concepts, formulas, real-world analogies, aur mathematics step-by-step solutions ke baare me pooch sakte hain! \n\n**Try asking:**\n- *'Differentiation Class 12 Math'*\n- *'Acids and Bases Class 10 Chemistry'*\n- *'Constitutional Rights of Civics'*\n- *'French Revolution Class 9 History'*\n\n*Hum aage kya padhna start karein?*",
                        isQuiz = true,
                        quizQuestion = "Which celestial body is at the center of our solar system, providing light and heat energy?",
                        quizOptions = listOf("A) Earth", "B) Jupiter", "C) The Sun", "D) The Moon"),
                        correctAnswerIndex = 2
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuterChatScreen(navController: NavController, prompt: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory)
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    
    val userName = remember(currentUser) {
        currentUser?.name?.split(" ")?.firstOrNull() ?: "Student"
    }

    val repository = remember { com.example.data.repository.AuraRepository() }
    val db = remember { com.example.data.local.PlannerDatabase.getDatabase(context) }

    // Live Flow updates collected directly as Compose States from SQLite
    val sessionsList by db.studySessionDao().getAllSessions().collectAsState(initial = emptyList())
    val examsList by db.examDateSheetDao().getAllExamsFlow().collectAsState(initial = emptyList())
    val calculatorHistory by db.calculatorHistoryDao().getAllHistory().collectAsState(initial = emptyList())

    var booksList by remember { mutableStateOf<List<com.example.data.models.Book>>(emptyList()) }
    var coursesList by remember { mutableStateOf<List<com.example.data.models.Course>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            booksList = repository.getBooks()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            coursesList = repository.getCourses()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // In-memory local message log state
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isAuraThinking by remember { mutableStateOf(false) }
    var userText by remember { mutableStateOf("") }

    // On-boarding welcome greetings
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(
                ChatMessage(
                    role = "assistant",
                    content = "### Hello, $userName! 👋\n\nI am your personal **Aura AI Tutor**, powered natively on your device. I can help you solve homework, clarify complex doubts, write detailed summaries, practice interactive quizzes, and compose essays!\n\n**Select a fast suggestion shortcut below or type your question!**"
                )
            )
            
            // If we have an incoming deep-link prompt from the study tools screen, execute it automatically!
            if (!prompt.isNullOrBlank()) {
                isAuraThinking = true
                delay(800L) // UI elegance delay
                messages.add(
                    ChatMessage(
                        role = "user",
                        content = prompt
                    )
                )
                delay(1200L) // Simulating deep intellectual thinking
                val auraReply = getAuraResponse(
                    input = prompt,
                    userName = userName,
                    booksList = booksList,
                    coursesList = coursesList,
                    examsList = examsList,
                    sessionsList = sessionsList,
                    calculatorHistory = calculatorHistory
                )
                messages.add(auraReply)
                isAuraThinking = false
                
                // Scroll down smoothly
                coroutineScope.launch {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }

    // Helper to send a message
    val sendMessageAction: (String) -> Unit = { textToSend ->
        if (textToSend.isNotBlank()) {
            messages.add(
                ChatMessage(
                    role = "user",
                    content = textToSend
                )
            )
            userText = ""
            isAuraThinking = true
            
            // Auto-scroll after sending
            coroutineScope.launch {
                delay(100L)
                listState.animateScrollToItem(messages.size - 1)
            }

            coroutineScope.launch {
                delay(1200L) // Elegant responsive simulation delay
                val botResponse = getAuraResponse(
                    input = textToSend,
                    userName = userName,
                    booksList = booksList,
                    coursesList = coursesList,
                    examsList = examsList,
                    sessionsList = sessionsList,
                    calculatorHistory = calculatorHistory
                )
                messages.add(botResponse)
                isAuraThinking = false
                
                // Auto-scroll after reply
                delay(100L)
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Aura AI Tutor", 
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Ready Offline", 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Native collapsible Pomodoro timer
            PomodoroTimerWidget()

            // Main chat messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
            ) {
                itemsIndexed(messages, key = { _, message -> message.id }) { index, msg ->
                    val isAi = msg.role == "assistant"
                    val bubbleBg = if (isAi) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                    val textColor = if (isAi) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End,
                        verticalAlignment = Alignment.Top
                    ) {
                        // AI avatar indicator
                        if (isAi) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp, end = 8.dp, top = 4.dp)
                                    .size(32.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "Aura AI Avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Message bubble card
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .background(
                                    color = bubbleBg,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isAi) 4.dp else 16.dp,
                                        bottomEnd = if (isAi) 16.dp else 4.dp
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            // Rich Text Content
                            Text(
                                text = parseMarkdown(msg.content, MaterialTheme.colorScheme.primary),
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                                lineHeight = 22.sp
                            )

                            // Interactive quiz options layout (if it's a practice quiz)
                            if (msg.isQuiz && msg.quizOptions != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg.quizQuestion ?: "",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    msg.quizOptions.forEachIndexed { optIndex, optionText ->
                                        val isSelected = msg.selectedOptionIndex == optIndex
                                        val isCorrect = msg.correctAnswerIndex == optIndex
                                        val quizOptionColor = when {
                                            msg.answered && isCorrect -> Color(0xFFE8F5E9) // correct highlights in green
                                            msg.answered && isSelected -> Color(0xFFFFEBEE) // incorrect chosen is red
                                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                        val quizOptionBorderColor = when {
                                            msg.answered && isCorrect -> Color(0xFF4CAF50)
                                            msg.answered && isSelected -> Color(0xFFE57373)
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        }
                                        val quizOptionTextColor = when {
                                            msg.answered && isCorrect -> Color(0xFF2E7D32)
                                            msg.answered && isSelected -> Color(0xFFC62828)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }

                                        Surface(
                                            onClick = {
                                                if (!msg.answered) {
                                                    val feedbackText = if (optIndex == msg.correctAnswerIndex) {
                                                        "🎉 Correct! Mitochondria generates ATP (Adenosine Triphosphate) through cellular respiration."
                                                    } else {
                                                        "❌ Incorrect. The powerhouse of the cell is the Mitochondria (Option B), which generates chemical energy."
                                                    }
                                                    
                                                    // Update the message in state dynamically
                                                    messages[index] = msg.copy(
                                                        selectedOptionIndex = optIndex,
                                                        answered = true,
                                                        quizFeedback = feedbackText
                                                    )
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = quizOptionColor,
                                            border = BorderStroke(1.dp, quizOptionBorderColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = null, // Handled by surface click
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = if (msg.answered && isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = optionText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = quizOptionTextColor,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    // Display explanation feedback if answered
                                    if (msg.answered && msg.quizFeedback != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (msg.selectedOptionIndex == msg.correctAnswerIndex) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = msg.quizFeedback,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (msg.selectedOptionIndex == msg.correctAnswerIndex) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                modifier = Modifier.padding(10.dp),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // User spacer to push bubble
                        if (!isAi) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 4.dp, top = 4.dp)
                                    .size(32.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Avatar",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Smooth typing pulse indicator
                if (isAuraThinking) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Quick Suggestions horizontal helper chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionShortcutChip("Solve Quadratic Equation 📝") { sendMessageAction("Solve x² - 5x + 6 = 0") }
                SuggestionShortcutChip("Explain Photosynthesis 🌿") { sendMessageAction("Explain Photosynthesis process") }
                SuggestionShortcutChip("Explain Gravity 🌌") { sendMessageAction("Explain Gravity formula") }
                SuggestionShortcutChip("Quiz Me! 🎯") { sendMessageAction("Give me an MCQ practice quiz") }
                SuggestionShortcutChip("Essay Outline: AI 🖋️") { sendMessageAction("Essay outline for Role of AI in Education") }
            }

            // Bottom Chat input box bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userText,
                        onValueChange = { userText = it },
                        placeholder = { Text("Ask Aura AI custom study doubts...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        trailingIcon = {
                            if (userText.isNotEmpty()) {
                                IconButton(onClick = { userText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    FloatingActionButton(
                        onClick = {
                            if (userText.isNotBlank()) {
                                sendMessageAction(userText)
                            }
                        },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionShortcutChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
