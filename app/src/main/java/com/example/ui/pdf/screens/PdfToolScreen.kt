package com.example.ui.pdf.screens

import android.Manifest
import android.os.Build
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PdfToolScreen(navController: NavController, viewModel: PdfToolViewModel = viewModel()) {
    val context = LocalContext.current
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES) // Adjust for generic files
    } else {
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val pdfFiles by viewModel.pdfFiles.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(storagePermission.status.isGranted) {
        if (storagePermission.status.isGranted) {
            viewModel.scanForPdfs(context)
        }
    }

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
        if (!storagePermission.status.isGranted) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { storagePermission.launchPermissionRequest() }) {
                    Text("Grant Storage Permission")
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    label = { Text("Search PDFs") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                )
                
                LazyColumn {
                    items(pdfFiles.filter { it.name.contains(searchQuery, ignoreCase = true) }) { pdf ->
                        ListItem(
                            headlineContent = { Text(pdf.name) },
                            supportingContent = { Text("${pdf.size / 1024} KB") },
                            modifier = Modifier.clickable { 
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", java.io.File(pdf.path))
                                intent.setDataAndType(uri, "application/pdf")
                                intent.flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}
