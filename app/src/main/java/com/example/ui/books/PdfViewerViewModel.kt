package com.example.ui.books

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PdfAnnotation
import com.example.data.local.PlannerDatabase
import com.example.data.repository.PdfAnnotationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PdfViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: PdfAnnotationRepository
    
    init {
        val db = PlannerDatabase.getDatabase(application)
        repo = PdfAnnotationRepository(db.pdfAnnotationDao())
    }

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress

    private val _pdfPageCount = MutableStateFlow(0)
    val pdfPageCount: StateFlow<Int> = _pdfPageCount

    private val _annotations = MutableStateFlow<List<PdfAnnotation>>(emptyList())
    val annotations: StateFlow<List<PdfAnnotation>> = _annotations

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    fun loadPdf(url: String, bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Collect annotations
                launch {
                    repo.getAnnotationsForBook(bookId).collect {
                        _annotations.value = it
                    }
                }

                val file = File(getApplication<Application>().cacheDir, "book_$bookId.pdf")
                if (!file.exists()) {
                    _downloadProgress.value = 0f
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
                    
                    if (!response.isSuccessful) throw Exception("Failed to download PDF")
                    
                    val body = response.body ?: throw Exception("Empty body")
                    val contentLength = body.contentLength()
                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(file)
                    
                    var bytesCopied = 0L
                    val buffer = ByteArray(8 * 1024)
                    var bytes = inputStream.read(buffer)
                    while (bytes >= 0) {
                        outputStream.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        if (contentLength > 0) {
                            _downloadProgress.value = bytesCopied.toFloat() / contentLength
                        }
                        bytes = inputStream.read(buffer)
                    }
                    outputStream.close()
                    inputStream.close()
                }

                _downloadProgress.value = 1f
                
                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor!!)
                _pdfPageCount.value = pdfRenderer!!.pageCount
                
            } catch (e: Exception) {
                Log.e("PdfViewerViewModel", "Error loading PDF", e)
                _downloadProgress.value = -1f // Error state
            }
        }
    }

    suspend fun renderPage(pageIndex: Int, width: Int): Bitmap? = withContext(Dispatchers.IO) {
        if (pdfRenderer == null || pageIndex < 0 || pageIndex >= pdfRenderer!!.pageCount) return@withContext null
        
        try {
            val page = pdfRenderer!!.openPage(pageIndex)
            val height = (width.toFloat() / page.width * page.height).toInt()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            // Fill white background
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap
        } catch (e: Exception) {
            Log.e("PdfViewerViewModel", "Error rendering page $pageIndex", e)
            null
        }
    }

    fun addAnnotation(annotation: PdfAnnotation) {
        viewModelScope.launch {
            repo.insertAnnotation(annotation)
        }
    }

    fun deleteAnnotation(id: String) {
        viewModelScope.launch {
            repo.deleteAnnotationById(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pdfRenderer?.close()
        fileDescriptor?.close()
    }
}
