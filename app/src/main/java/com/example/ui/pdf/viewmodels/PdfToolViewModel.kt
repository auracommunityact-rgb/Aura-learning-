package com.example.ui.pdf.viewmodels

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.pdf.models.PdfFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfToolViewModel : ViewModel() {
    private val _pdfFiles = MutableStateFlow<List<PdfFile>>(emptyList())
    val pdfFiles = _pdfFiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun scanForPdfs(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _pdfFiles.value = withContext(Dispatchers.IO) {
                val files = mutableListOf<PdfFile>()
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED
                )
                val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
                val selectionArgs = arrayOf("application/pdf")
                
                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

                    while (cursor.moveToNext()) {
                        files.add(
                            PdfFile(
                                cursor.getLong(idCol),
                                cursor.getString(nameCol),
                                cursor.getString(pathCol),
                                cursor.getLong(sizeCol),
                                cursor.getLong(dateCol)
                            )
                        )
                    }
                }
                files
            }
            _isLoading.value = false
        }
    }
}
