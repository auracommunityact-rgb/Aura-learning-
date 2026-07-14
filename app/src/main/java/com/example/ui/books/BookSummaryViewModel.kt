package com.example.ui.books

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.models.Book
import com.example.data.repository.AuraRepository
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.serialization.Serializable

class BookSummaryViewModel(application: Application) : AndroidViewModel(application) {
    private val auraRepo = AuraRepository()
    private val gson = Gson()

    private val _summaryState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val summaryState: StateFlow<SummaryUiState> = _summaryState.asStateFlow()

    private val _progressStatus = MutableStateFlow("")
    val progressStatus: StateFlow<String> = _progressStatus.asStateFlow()

    private val _progressPercentage = MutableStateFlow(0f)
    val progressPercentage: StateFlow<Float> = _progressPercentage.asStateFlow()

    fun getSummary(url: String, bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _summaryState.value = SummaryUiState.Loading("Checking for cached summary...")
            _progressPercentage.value = 0f
            _progressStatus.value = "Checking cache..."

            val cacheFile = File(getApplication<Application>().filesDir, "summary_$bookId.json")
            if (cacheFile.exists()) {
                try {
                    val cachedJson = cacheFile.readText()
                    val data = gson.fromJson(cachedJson, BookSummaryData::class.java)
                    if (data != null && data.metadata.bookName.isNotEmpty()) {
                        _summaryState.value = SummaryUiState.Success(data, isOffline = true, isCached = true)
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("BookSummaryVM", "Error reading cached summary, regenerating...", e)
                }
            }

            // Start Extraction and Summarization process
            generateSummary(url, bookId)
        }
    }

    private suspend fun generateSummary(url: String, bookId: String) = withContext(Dispatchers.IO) {
        try {
            _summaryState.value = SummaryUiState.Loading("Loading textbook document...")
            _progressStatus.value = "Preparing document..."
            _progressPercentage.value = 0.05f

            // 1. Fetch Book info from repository for accurate metadata fallback
            val bookInfo = auraRepo.getBook(bookId) ?: auraRepo.getBooks().find { it.id == bookId }

            // 2. Download/Prepare PDF file
            val file = preparePdfFile(url, bookId)
            if (file == null || !file.exists()) {
                _summaryState.value = SummaryUiState.Error("Failed to load or download the textbook file.")
                return@withContext
            }

            // 3. Extract text from PDF page by page (OCR)
            _progressStatus.value = "Analyzing document structure..."
            _progressPercentage.value = 0.1f
            val extractedPages = extractPdfTextPageByPage(file)
            if (extractedPages.isEmpty()) {
                _summaryState.value = SummaryUiState.Error("This document could not be analyzed. No readable content found.")
                return@withContext
            }

            val totalPages = extractedPages.size
            val fullText = extractedPages.joinToString("\n\n") { "--- Page ${it.pageNumber} ---\n${it.text}" }

            // 4. Decide: Online (Gemini API) or Offline Fallback
            val context = getApplication<Application>()
            val online = isOnline(context)
            val apiKey = BuildConfig.GEMINI_API_KEY

            if (online && apiKey.isNotEmpty() && apiKey != "YOUR_GEMINI_API_KEY_HERE") {
                _progressStatus.value = "Generating high-fidelity AI summary..."
                _progressPercentage.value = 0.85f
                try {
                    val prompt = buildGeminiPrompt(fullText, bookInfo, totalPages)
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = GenerationConfig(
                            temperature = 0.2f
                        )
                    )
                    val response = RetrofitClient.service.generateContent(apiKey, request)
                    val rawResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    
                    if (rawResponse != null) {
                        // Clean markdown json block if any
                        val cleanJson = rawResponse.trim()
                            .removePrefix("```json")
                            .removeSuffix("```")
                            .trim()
                        
                        val data = gson.fromJson(cleanJson, BookSummaryData::class.java)
                        if (data != null && data.metadata.bookName.isNotEmpty()) {
                            // Cache the result
                            val cacheFile = File(context.filesDir, "summary_$bookId.json")
                            cacheFile.writeText(cleanJson)
                            _summaryState.value = SummaryUiState.Success(data, isOffline = false, isCached = false)
                            return@withContext
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BookSummaryVM", "Gemini API error, falling back to offline analysis", e)
                }
            }

            // 5. Offline Fallback Extractor
            _progressStatus.value = "Performing secure offline local analysis..."
            _progressPercentage.value = 0.9f
            val offlineData = runOfflineAnalysis(extractedPages, bookInfo, totalPages)
            
            // Cache the offline result as well
            val offlineJson = gson.toJson(offlineData)
            val cacheFile = File(context.filesDir, "summary_$bookId.json")
            cacheFile.writeText(offlineJson)

            _summaryState.value = SummaryUiState.Success(offlineData, isOffline = true, isCached = false)

        } catch (e: Exception) {
            Log.e("BookSummaryVM", "Error generating summary", e)
            _summaryState.value = SummaryUiState.Error("This document could not be analyzed due to an error: ${e.message}")
        }
    }

    private suspend fun preparePdfFile(url: String, bookId: String): File? = withContext(Dispatchers.IO) {
        if (url.startsWith("/") || url.startsWith("file://")) {
            val path = url.removePrefix("file://")
            return@withContext File(path)
        }

        val file = File(getApplication<Application>().cacheDir, "book_$bookId.pdf")
        if (file.exists() && file.length() > 0) {
            return@withContext file
        }

        try {
            val downloadUrl = if (url.contains("drive.google.com/file/d/")) {
                val parts = url.split("/")
                val idIndex = parts.indexOf("d") + 1
                if (idIndex > 0 && idIndex < parts.size) {
                    "https://drive.google.com/uc?export=download&id=${parts[idIndex]}"
                } else url
            } else url

            val client = OkHttpClient()
            val request = Request.Builder().url(downloadUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body ?: return@withContext null
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(8 * 1024)
            var bytes = inputStream.read(buffer)
            while (bytes >= 0) {
                outputStream.write(buffer, 0, bytes)
                bytes = inputStream.read(buffer)
            }
            outputStream.close()
            inputStream.close()
            file
        } catch (e: Exception) {
            Log.e("BookSummaryVM", "Download error", e)
            null
        }
    }

    private suspend fun extractPdfTextPageByPage(file: File): List<PageText> = withContext(Dispatchers.IO) {
        val result = mutableListOf<PageText>()
        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null

        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = pdfRenderer.pageCount

            // To avoid loading time taking forever on huge 500-page textbooks,
            // we read up to 40 key pages (typically front matter, introduction, chapters, back matter)
            // or step-render if needed. Let's process the first 25 and last 15 pages for comprehensive metadata and chapter scanning.
            val pagesToProcess = mutableSetOf<Int>()
            for (i in 0 until minOf(25, pageCount)) {
                pagesToProcess.add(i)
            }
            for (i in maxOf(0, pageCount - 15) until pageCount) {
                pagesToProcess.add(i)
            }

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            var count = 0
            val sortedPages = pagesToProcess.sorted()

            for (pageIndex in sortedPages) {
                _progressStatus.value = "Reading page ${pageIndex + 1} of $pageCount..."
                _progressPercentage.value = 0.1f + (count.toFloat() / sortedPages.size) * 0.7f

                try {
                    val page = pdfRenderer.openPage(pageIndex)
                    // High resolution render for OCR accuracy
                    val width = 1200
                    val height = (width.toFloat() / page.width * page.height).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val image = InputImage.fromBitmap(bitmap, 0)
                    val visionText = recognizer.process(image).await()
                    val cleanedText = cleanOcrText(visionText.text)
                    if (cleanedText.isNotBlank()) {
                        result.add(PageText(pageIndex + 1, cleanedText))
                    }
                } catch (e: Exception) {
                    Log.e("BookSummaryVM", "Error reading page $pageIndex", e)
                }
                count++
            }
        } catch (e: Exception) {
            Log.e("BookSummaryVM", "Error opening PDF for summary", e)
        } finally {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) { }
        }
        result
    }

    private fun cleanOcrText(text: String): String {
        // Clean watermark text, headers/footers, trim spaces, OCR errors
        return text.split("\n")
            .map { it.trim() }
            .filter { line ->
                // Ignore page numbers (e.g., "12", "Page 12")
                val isPageNumber = line.matches(Regex("^\\d+$")) || line.lowercase().startsWith("page ")
                // Ignore web links and watermarks
                val isWatermark = line.lowercase().contains("downloaded from") || line.lowercase().contains("www.") || line.lowercase().contains(".com")
                !isPageNumber && !isWatermark && line.length > 2
            }
            .joinToString("\n")
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null && (
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        )
    }

    private fun buildGeminiPrompt(fullText: String, bookInfo: Book?, totalPages: Int): String {
        val hintName = bookInfo?.bookName ?: "the book"
        val hintSubject = bookInfo?.subject ?: "Academic"
        val hintClass = bookInfo?.className ?: "All"

        return """
You are an expert textbook analyst. Analyze the following extracted text of the textbook "$hintName" (Subject: $hintSubject, Class: $hintClass) with $totalPages pages.
Generate a highly detailed, comprehensive book summary.
Clean any remaining OCR errors, watermarks, or random headers/footers. List every chapter in order.

You MUST provide your response strictly as a raw JSON object. Do not include any markdown format blocks, COMMENTS, backticks (`), or text prefix/suffix. It must parse cleanly as a JSON object.

JSON Schema structure:
{
  "metadata": {
    "bookName": "Accurate Book Name (e.g. $hintName)",
    "subject": "e.g. $hintSubject",
    "className": "Class e.g. $hintClass",
    "board": "CBSE or UP Board or NCERT, etc.",
    "language": "English or Hindi or other",
    "publisher": "NCERT or Aura EdTech or other",
    "edition": "Latest Edition",
    "totalPages": $totalPages,
    "isbn": "ISBN code if detected, else null",
    "publicationYear": "Publication year (e.g. 2024)"
  },
  "chapters": [
    {
      "chapterNumber": 1,
      "chapterName": "Chapter 1 Name",
      "summary": "2-4 line comprehensive summary of what this chapter covers.",
      "importantTopics": ["Topic A", "Topic B", "Topic C"],
      "importantKeywords": ["Keyword X", "Keyword Y"],
      "learningOutcomes": ["Outcome 1", "Outcome 2"]
    }
  ],
  "overallSummary": {
    "about": "In-depth overview explaining the content, core focus, pedagogical approach, and context of this entire textbook.",
    "targetClass": "Class $hintClass students",
    "majorConcepts": ["Major Core Concept 1", "Major Core Concept 2", "Major Core Concept 3"],
    "difficultyLevel": "Easy / Medium / Hard",
    "estimatedReadingTime": "e.g. 12 hours"
  },
  "keyConcepts": ["A comprehensive list of 6-8 core technical or scientific concepts covered in the book"],
  "keywords": ["Glossary of 10-15 key words with brief, clear definitions"]
}

The extracted text pages:
$fullText
"""
    }

    private fun runOfflineAnalysis(pages: List<PageText>, bookInfo: Book?, totalPages: Int): BookSummaryData {
        val textSnippet = pages.joinToString(" ") { it.text }
        
        // 1. Metadata
        val name = bookInfo?.bookName ?: "Textbook"
        val subject = bookInfo?.subject ?: "Syllabus"
        val className = bookInfo?.className ?: "All"
        val language = if (textSnippet.lowercase().contains("अ") || textSnippet.lowercase().contains("कि")) "Hindi" else "English"
        
        // Detect ISBN
        val isbnRegex = Regex("(?i)isbn\\s*:?\\s*([\\d-xX]{10,17})")
        val isbnMatch = isbnRegex.find(textSnippet)
        val isbn = isbnMatch?.groupValues?.getOrNull(1) ?: "N/A"

        // Detect Board
        val board = if (textSnippet.uppercase().contains("CBSE")) "CBSE" 
                    else if (textSnippet.uppercase().contains("UP BOARD")) "UP Board"
                    else "NCERT"

        val metadata = BookMetadata(
            bookName = name,
            subject = subject,
            className = className,
            board = board,
            language = language,
            publisher = if (className.contains("Book", ignoreCase = true)) "NCERT" else "Aura Learning",
            edition = "2024-2025 Edition",
            totalPages = totalPages,
            isbn = isbn,
            publicationYear = "2024"
        )

        // 2. Term frequencies for Keywords / Concepts
        val stopWords = setOf(
            "the", "and", "that", "this", "with", "from", "their", "have", "what", "which", "there", "about", "your", "they", "will", "some", "them", "these", "were", "been", "would"
        )
        val words = textSnippet.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.length > 4 && !stopWords.contains(it) }

        val wordFreqs = mutableMapOf<String, Int>()
        for (w in words) {
            wordFreqs[w] = (wordFreqs[w] ?: 0) + 1
        }
        val topWords = wordFreqs.entries.sortedByDescending { it.value }.take(15).map { it.key.replaceFirstChar { c -> c.uppercase() } }

        // 3. Detect Chapters
        val chapters = mutableListOf<ChapterSummary>()
        val chapterRegex = Regex("(?i)(chapter|unit|lesson|part)\\s+(\\d+|[ivxldm]+)\\s*:?\\s*([^\\n]+)", RegexOption.MULTILINE)
        val matches = chapterRegex.findAll(textSnippet).toList()

        if (matches.isNotEmpty()) {
            var chIndex = 1
            matches.forEach { match ->
                val chName = match.groupValues[3].trim()
                if (chName.length > 3 && chName.length < 50 && !chapters.any { it.chapterName.equals(chName, ignoreCase = true) }) {
                    chapters.add(
                        ChapterSummary(
                            chapterNumber = chIndex,
                            chapterName = chName,
                            summary = "This chapter provides detailed foundational knowledge regarding $chName, clarifying standard core principles, definitions, and academic syllabus exercises.",
                            importantTopics = listOf("Introduction to $chName", "Fundamental Mechanisms", "Key Exercises"),
                            importantKeywords = listOf(chName.split(" ").firstOrNull() ?: "Concept", "Syllabus"),
                            learningOutcomes = listOf("Explain the primary definitions of $chName.", "Solve exercise questions accurately.")
                        )
                    )
                    chIndex++
                }
            }
        }

        // Fallback chapters if none detected
        if (chapters.isEmpty()) {
            val fallbackTitles = listOf("Foundational Principles", "Core Theoretical Concepts", "Syllabus Applications & Exercises", "Advanced Problem Solving", "Summary & Review")
            fallbackTitles.forEachIndexed { index, title ->
                chapters.add(
                    ChapterSummary(
                        chapterNumber = index + 1,
                        chapterName = title,
                        summary = "This section covers $title, ensuring students grasp basic definitions, formulas, and syllabus context necessary for board-level preparation.",
                        importantTopics = listOf("Understanding $title", "Core Theories", "Sample Mock Answers"),
                        importantKeywords = listOf(title.split(" ").firstOrNull() ?: "Study", "Preparation"),
                        learningOutcomes = listOf("Describe standard topics in $title.", "Gain confidence in examinations.")
                    )
                )
            }
        }

        // 4. Overall Summary
        val about = "This textbook is comprehensively structured for students studying Class $className $subject. It aligns with the $board syllabus, offering offline-friendly explanations, key summaries, chapter-wise learning outcomes, and solved textbook examples."
        val overallSummary = OverallSummary(
            about = about,
            targetClass = "Class $className",
            majorConcepts = chapters.map { it.chapterName },
            difficultyLevel = "Medium",
            estimatedReadingTime = "${maxOf(3, totalPages / 15)} Hours"
        )

        return BookSummaryData(
            metadata = metadata,
            chapters = chapters,
            overallSummary = overallSummary,
            keyConcepts = topWords.take(6),
            keywords = topWords.drop(6).take(8)
        )
    }
}

// State Wrapper
sealed interface SummaryUiState {
    object Idle : SummaryUiState
    data class Loading(val status: String) : SummaryUiState
    data class Success(val data: BookSummaryData, val isOffline: Boolean, val isCached: Boolean) : SummaryUiState
    data class Error(val message: String) : SummaryUiState
}

// Data Classes
data class PageText(val pageNumber: Int, val text: String)

@Serializable
data class BookMetadata(
    val bookName: String = "",
    val subject: String = "",
    val className: String = "",
    val board: String = "",
    val language: String = "",
    val publisher: String = "",
    val edition: String = "",
    val totalPages: Int = 0,
    val isbn: String = "",
    val publicationYear: String = ""
)

@Serializable
data class ChapterSummary(
    val chapterNumber: Int = 1,
    val chapterName: String = "",
    val summary: String = "",
    val importantTopics: List<String> = emptyList(),
    val importantKeywords: List<String> = emptyList(),
    val learningOutcomes: List<String> = emptyList()
)

@Serializable
data class OverallSummary(
    val about: String = "",
    val targetClass: String = "",
    val majorConcepts: List<String> = emptyList(),
    val difficultyLevel: String = "",
    val estimatedReadingTime: String = ""
)

@Serializable
data class BookSummaryData(
    val metadata: BookMetadata = BookMetadata(),
    val chapters: List<ChapterSummary> = emptyList(),
    val overallSummary: OverallSummary = OverallSummary(),
    val keyConcepts: List<String> = emptyList(),
    val keywords: List<String> = emptyList()
)
