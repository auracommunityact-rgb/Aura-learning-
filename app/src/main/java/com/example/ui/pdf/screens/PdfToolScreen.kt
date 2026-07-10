package com.example.ui.pdf.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.pdf.viewmodels.PdfToolViewModel
import com.example.ui.pdfmanager.HorizontalPdfReader
import com.example.ui.pdfmanager.StoragePermissionWrapper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToolScreen(navController: NavController, viewModel: PdfToolViewModel = viewModel()) {
    val context = LocalContext.current
    val pdfFiles by viewModel.pdfFiles.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }

    if (selectedPdfUri != null) {
        BackHandler {
            selectedPdfUri = null
        }
        HorizontalPdfReader(
            uri = selectedPdfUri!!,
            onClose = { selectedPdfUri = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PDF Reader & Builder") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("pdf_builder") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create PDF")
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                StoragePermissionWrapper {
                    // Once permission is granted, trigger PDF scanning
                    LaunchedEffect(Unit) {
                        viewModel.scanForPdfs(context)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            label = { Text("Search PDFs") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                        )

                        if (pdfFiles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No PDF files found on device.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(pdfFiles.filter { it.name.contains(searchQuery, ignoreCase = true) }) { pdf ->
                                    ListItem(
                                        headlineContent = { Text(pdf.name) },
                                        supportingContent = { Text("${pdf.size / 1024} KB") },
                                        modifier = Modifier.clickable {
                                            val file = File(pdf.path)
                                            if (file.exists()) {
                                                selectedPdfUri = Uri.fromFile(file)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
