package com.example.ui.study

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.repository.AuraRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

// Local copy of parseMarkdown to keep the file fully self-contained and modular
fun parseMarkdownLocal(text: String, primaryColor: Color): androidx.compose.ui.text.AnnotatedString {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultAnalysisScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AuraRepository() }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<AnalysisData?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var aiFeedback by remember { mutableStateOf("") }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            analysisResult = null
            aiFeedback = ""
            errorMessage = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Result Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Selected Result",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to select report card / result photo", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (bitmap == null) "Select Photo" else "Change Photo")
            }

            if (bitmap != null) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isAnalyzing = true
                            errorMessage = ""
                            aiFeedback = ""
                            try {
                                // 1. Run local ML Kit OCR for standard score calculations
                                val image = InputImage.fromBitmap(bitmap!!, 0)
                                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                val result = recognizer.process(image).await()
                                val extractedText = result.text
                                
                                // Parse text to find marks and subjects
                                analysisResult = analyzeResultText(extractedText)
                                
                                // 2. Upload to Supabase in background
                                withContext(Dispatchers.IO) {
                                    val stream = ByteArrayOutputStream()
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                                    val bytes = stream.toByteArray()
                                    val fileName = "result_${UUID.randomUUID()}.jpg"
                                    repository.uploadResultImage(bytes, fileName)
                                }

                                // 3. Call Gemini 3.5 Flash for advanced vision/text OCR and analysis
                                try {
                                    val stream = ByteArrayOutputStream()
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                                    val imageBytes = stream.toByteArray()
                                    val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                                    
                                    val apiKey = BuildConfig.GEMINI_API_KEY
                                    val prompt = "Analyze this school report card or exam result sheet. Extract all text, subjects, marks/grades, and provide a highly detailed, encouraging, and clear analysis in Hindi and English (mix of Hindi & English is best). Highlight strong areas, weak areas, and write a customized step-by-step study strategy to help this student study better and score top marks in upcoming exams."
                                    
                                    val request = GenerateContentRequest(
                                        contents = listOf(
                                            Content(
                                                parts = listOf(
                                                    Part(text = prompt),
                                                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                                                )
                                            )
                                        )
                                    )
                                    val response = RetrofitClient.service.generateContent(apiKey, request)
                                    aiFeedback = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Could not generate automated analysis feedback."
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    aiFeedback = "AI Analysis Feedback Error: ${e.message}\n(Please check your internet connection or Gemini API key in Secrets)"
                                }
                                
                            } catch (e: Exception) {
                                e.printStackTrace()
                                errorMessage = "Failed to analyze result: ${e.message}"
                            } finally {
                                isAnalyzing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Filled.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze & Understand with Gemini AI")
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }

            analysisResult?.let { result ->
                ResultCard(result)
            }

            if (aiFeedback.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Gemini AI's Deep Insights ✨",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        Text(
                            text = parseMarkdownLocal(aiFeedback, MaterialTheme.colorScheme.primary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

data class SubjectScore(val name: String, val score: Int)
data class AnalysisData(
    val scores: List<SubjectScore>,
    val totalScore: Int,
    val totalPossible: Int,
    val percentage: Double,
    val weakSubjects: List<String>,
    val strongSubjects: List<String>
)

fun analyzeResultText(text: String): AnalysisData {
    val knownSubjects = listOf(
        "English", "Hindi", "Mathematics", "Maths", "Science",
        "Social Science", "SST", "Physics", "Chemistry", "Biology",
        "Computer", "History", "Geography", "Economics"
    )
    
    val foundScores = mutableListOf<SubjectScore>()
    val lines = text.split("\n")
    
    val numberRegex = Regex("\\b([0-9]{1,3})\\b")
    
    for (subject in knownSubjects) {
        var scoreForSubject = -1
        for (i in lines.indices) {
            if (lines[i].contains(subject, ignoreCase = true)) {
                // Check same line for a number
                val nums = numberRegex.findAll(lines[i]).map { it.value.toInt() }.filter { it <= 100 && it > 0 }.toList()
                if (nums.isNotEmpty()) {
                    scoreForSubject = nums.maxOrNull() ?: -1
                    break
                }
                // Check next line
                if (i + 1 < lines.size) {
                    val nextNums = numberRegex.findAll(lines[i + 1]).map { it.value.toInt() }.filter { it <= 100 && it > 0 }.toList()
                    if (nextNums.isNotEmpty()) {
                        scoreForSubject = nextNums.maxOrNull() ?: -1
                        break
                    }
                }
            }
        }
        
        // Ensure uniqueness
        if (scoreForSubject != -1 && foundScores.none { it.name.contains(subject, true) || subject.contains(it.name, true) }) {
            foundScores.add(SubjectScore(subject, scoreForSubject))
        }
    }
    
    val totalScore = foundScores.sumOf { it.score }
    val totalPossible = foundScores.size * 100
    val percentage = if (totalPossible > 0) (totalScore.toDouble() / totalPossible) * 100 else 0.0
    
    val weakSubjects = foundScores.filter { it.score < 60 }.map { it.name }
    val strongSubjects = foundScores.filter { it.score >= 80 }.map { it.name }
    
    return AnalysisData(foundScores, totalScore, totalPossible, percentage, weakSubjects, strongSubjects)
}

@Composable
fun ResultCard(data: AnalysisData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Extracted Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            if (data.scores.isEmpty()) {
                Text("Searching marks in the image... ML Kit fast-search was empty, relying fully on Gemini detailed AI insights below!")
                return@Column
            }
            
            HorizontalDivider()
            
            Text("Scores:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            data.scores.forEach {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(it.name, fontWeight = FontWeight.Medium)
                    Text("${it.score} / 100", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            HorizontalDivider()
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${data.totalScore} / ${data.totalPossible}", fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Percentage:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(String.format("%.2f%%", data.percentage), fontWeight = FontWeight.Bold, color = if (data.percentage >= 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            
            HorizontalDivider()
            
            if (data.weakSubjects.isNotEmpty()) {
                Text("Needs Improvement:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Text(data.weakSubjects.joinToString(", "))
            }
            
            if (data.strongSubjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Strong Subjects:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(data.strongSubjects.joinToString(", "))
            }
        }
    }
}
