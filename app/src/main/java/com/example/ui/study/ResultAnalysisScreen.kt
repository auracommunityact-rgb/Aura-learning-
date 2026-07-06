package com.example.ui.study

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
            errorMessage = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result Analysis") },
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
                        .height(250.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to select result photo")
                        }
                    }
                }
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (bitmap == null) "Select Photo" else "Change Photo")
            }

            if (bitmap != null) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isAnalyzing = true
                            errorMessage = ""
                            try {
                                // 1. Run local ML Kit OCR
                                val image = InputImage.fromBitmap(bitmap!!, 0)
                                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                val result = recognizer.process(image).await()
                                val extractedText = result.text
                                
                                // 2. Parse text to find marks and subjects
                                analysisResult = analyzeResultText(extractedText)
                                
                                // 3. Upload to Supabase in background
                                withContext(Dispatchers.IO) {
                                    val stream = ByteArrayOutputStream()
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                                    val bytes = stream.toByteArray()
                                    val fileName = "result_${UUID.randomUUID()}.jpg"
                                    repository.uploadResultImage(bytes, fileName)
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
                    enabled = !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Filled.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze & Upload")
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            analysisResult?.let { result ->
                ResultCard(result)
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
    
    // Simple heuristic: look for subject name and a number <= 100 on the same line or next line
    val wordRegex = Regex("[a-zA-Z]+")
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
        
        // Ensure uniqueness (like Maths and Mathematics might both match, pick first found)
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
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Analysis Report", style = MaterialTheme.typography.titleLarge)
            
            if (data.scores.isEmpty()) {
                Text("Could not detect any subjects or marks in the image. Please try a clearer photo.")
                return@Column
            }
            
            HorizontalDivider()
            
            Text("Scores:", style = MaterialTheme.typography.titleMedium)
            data.scores.forEach {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(it.name)
                    Text("${it.score} / 100", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            HorizontalDivider()
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", style = MaterialTheme.typography.titleMedium)
                Text("${data.totalScore} / ${data.totalPossible}")
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Percentage:", style = MaterialTheme.typography.titleMedium)
                Text(String.format("%.2f%%", data.percentage), color = if (data.percentage >= 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            
            HorizontalDivider()
            
            if (data.weakSubjects.isNotEmpty()) {
                Text("Focus Areas (Needs Improvement):", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Text(data.weakSubjects.joinToString(", "))
            } else {
                Text("Great job! No specific weak subjects detected.", color = MaterialTheme.colorScheme.primary)
            }
            
            if (data.strongSubjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Strong Subjects:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(data.strongSubjects.joinToString(", "))
            }
        }
    }
}
