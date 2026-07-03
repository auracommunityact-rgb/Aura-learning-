package com.example.ui.books

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.local.PdfAnnotation
import com.example.data.local.PdfBookmark
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

enum class AnnotationTool {
    NONE, PEN, HIGHLIGHTER, UNDERLINE, ERASER
}

data class DrawnPath(
    val id: String = UUID.randomUUID().toString(),
    val type: AnnotationTool,
    val color: Color,
    val strokeWidth: Float,
    val path: List<Offset>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    navController: NavController,
    pdfUrl: String,
    bookId: String = "default_book",
    viewModel: PdfViewerViewModel = viewModel()
) {
    val progress by viewModel.downloadProgress.collectAsState()
    val pageCount by viewModel.pdfPageCount.collectAsState()
    val annotations by viewModel.annotations.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    
    val listState = rememberLazyListState()
    var currentTool by remember { mutableStateOf(AnnotationTool.NONE) }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var strokeWidth by remember { mutableStateOf(5f) }
    
    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color(0xFFFFA500), Color(0xFF800080), Color(0xFFFFC0CB), Color.Black)
    
    val undoStack = remember { mutableStateListOf<PdfAnnotation>() }
    
    var showSummaryDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var isSummarizing by remember { mutableStateOf(false) }
    var summaryText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    val context = LocalContext.current
    val startTime = remember { System.currentTimeMillis() }

    val handleBack = {
        val timeSpentMillis = System.currentTimeMillis() - startTime
        val minutes = (timeSpentMillis / 1000) / 60
        val seconds = (timeSpentMillis / 1000) % 60
        val timeString = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
        android.widget.Toast.makeText(context, "Time spent reading: $timeString", android.widget.Toast.LENGTH_SHORT).show()
        navController.popBackStack()
    }

    BackHandler {
        handleBack()
    }
    
    LaunchedEffect(pdfUrl, bookId) {
        viewModel.loadPdf(pdfUrl, bookId)
    }

    if (showBookmarksDialog) {
        AlertDialog(
            onDismissRequest = { showBookmarksDialog = false },
            title = { Text("Bookmarks") },
            text = {
                if (bookmarks.isEmpty()) {
                    Text("No bookmarks added yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    ) {
                        items(bookmarks.size) { index ->
                            val bookmark = bookmarks[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(bookmark.pageNumber)
                                            showBookmarksDialog = false
                                        }
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Page ${bookmark.pageNumber + 1}")
                                IconButton(onClick = { viewModel.toggleBookmark(bookId, bookmark.pageNumber) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove Bookmark")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookmarksDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSummarizing) showSummaryDialog = false },
            title = { Text("Page Summary") },
            text = {
                if (isSummarizing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Extracting text and generating summary...")
                    }
                } else {
                    Text(summaryText, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSummaryDialog = false }, enabled = !isSummarizing) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Reader") },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val currentPageIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
                    val isBookmarked = bookmarks.any { it.pageNumber == currentPageIndex }
                    IconButton(onClick = { viewModel.toggleBookmark(bookId, currentPageIndex) }) {
                        Icon(if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark Page", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showBookmarksDialog = true }) {
                        Icon(Icons.Filled.Bookmarks, contentDescription = "View Bookmarks", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        showSummaryDialog = true
                        isSummarizing = true
                        summaryText = ""
                        coroutineScope.launch {
                            val pageIndex = listState.firstVisibleItemIndex
                            summaryText = viewModel.summarizePage(pageIndex, 1080)
                            isSummarizing = false
                        }
                    }) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = "Summarize Page", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        val last = annotations.maxByOrNull { it.timestamp }
                        if (last != null) {
                            undoStack.add(last)
                            viewModel.deleteAnnotation(last.id)
                        }
                    }) {
                        Icon(Icons.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = {
                        val toRedo = undoStack.lastOrNull()
                        if (toRedo != null) {
                            undoStack.remove(toRedo)
                            viewModel.addAnnotation(toRedo)
                        }
                    }) {
                        Icon(Icons.Filled.Redo, contentDescription = "Redo")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentTool != AnnotationTool.NONE) {
                FloatingActionButton(onClick = { currentTool = AnnotationTool.NONE }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Tool")
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = currentTool != AnnotationTool.NONE) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentTool = AnnotationTool.PEN }) {
                            Icon(Icons.Filled.Create, contentDescription = "Pen", tint = if (currentTool == AnnotationTool.PEN) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                        }
                        IconButton(onClick = { currentTool = AnnotationTool.HIGHLIGHTER }) {
                            Icon(Icons.Filled.FormatColorFill, contentDescription = "Highlighter", tint = if (currentTool == AnnotationTool.HIGHLIGHTER) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                        }
                        IconButton(onClick = { currentTool = AnnotationTool.UNDERLINE }) {
                            Icon(Icons.Filled.FormatUnderlined, contentDescription = "Underline", tint = if (currentTool == AnnotationTool.UNDERLINE) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                        }
                        IconButton(onClick = { currentTool = AnnotationTool.ERASER }) {
                            Text("🧽", color = if (currentTool == AnnotationTool.ERASER) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                        }
                        
                        // Color picker simplified
                        colors.forEach { c ->
                            IconButton(
                                onClick = { selectedColor = c },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(c, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (progress == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (progress == -1f) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Error loading PDF", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadPdf(pdfUrl, bookId) }) {
                        Text("Retry")
                    }
                }
            } else if (progress!! < 1f) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(progress = progress!!)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Downloading... ${(progress!! * 100).toInt()}%")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = currentTool == AnnotationTool.NONE
                ) {
                    items(pageCount) { index ->
                        PdfPage(
                            pageIndex = index,
                            viewModel = viewModel,
                            annotations = annotations.filter { it.pageNumber == index },
                            currentTool = currentTool,
                            selectedColor = selectedColor,
                            strokeWidth = strokeWidth,
                            bookId = bookId
                        )
                    }
                }
            }
            
            if (currentTool == AnnotationTool.NONE) {
                FloatingActionButton(
                    onClick = { currentTool = AnnotationTool.PEN },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Create, contentDescription = "Annotate")
                }
            }
        }
    }
}

@Composable
fun PdfPage(
    pageIndex: Int,
    viewModel: PdfViewerViewModel,
    annotations: List<PdfAnnotation>,
    currentTool: AnnotationTool,
    selectedColor: Color,
    strokeWidth: Float,
    bookId: String
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var width by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    
    val gson = remember { Gson() }

    LaunchedEffect(width) {
        if (width > 0 && bitmap == null) {
            coroutineScope.launch(Dispatchers.IO) {
                bitmap = viewModel.renderPage(pageIndex, width)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { width = it.width }
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Page $pageIndex",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(currentTool) {
                        if (currentTool != AnnotationTool.NONE) {
                            detectDragGestures(
                                onDragStart = { currentPath = listOf(it) },
                                onDragEnd = {
                                    if (currentPath.isNotEmpty()) {
                                        if (currentTool == AnnotationTool.ERASER) {
                                            // Simple eraser logic: check bounding box
                                            val eraserRect = androidx.compose.ui.geometry.Rect(currentPath.first(), currentPath.last())
                                                .inflate(40f)
                                            annotations.forEach { ann ->
                                                val typeToken = object : TypeToken<List<Offset>>() {}.type
                                                val points: List<Offset> = gson.fromJson(ann.coordinates, typeToken)
                                                if (points.any { p -> eraserRect.contains(p) }) {
                                                    viewModel.deleteAnnotation(ann.id)
                                                }
                                            }
                                        } else {
                                            val annotation = PdfAnnotation(
                                                bookId = bookId,
                                                userId = "current_user",
                                                pageNumber = pageIndex,
                                                type = currentTool.name,
                                                color = selectedColor.value.toLong(),
                                                strokeWidth = if (currentTool == AnnotationTool.HIGHLIGHTER) 20f else strokeWidth,
                                                coordinates = gson.toJson(currentPath)
                                            )
                                            viewModel.addAnnotation(annotation)
                                        }
                                        currentPath = emptyList()
                                    }
                                },
                                onDragCancel = { currentPath = emptyList() },
                                onDrag = { change, _ ->
                                    currentPath = currentPath + change.position
                                }
                            )
                        }
                    }
            ) {
                // Draw saved annotations
                annotations.forEach { ann ->
                    val typeToken = object : TypeToken<List<Offset>>() {}.type
                    val points: List<Offset> = gson.fromJson(ann.coordinates, typeToken)
                    if (points.isNotEmpty()) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        
                        val alpha = if (ann.type == "HIGHLIGHTER") 0.4f else 1f
                        val color = Color(ann.color.toULong()).copy(alpha = alpha)
                        
                        if (ann.type == "UNDERLINE") {
                            // Draw straight line for underline
                            drawLine(
                                color = color,
                                start = Offset(points.first().x, points.first().y + 10f),
                                end = Offset(points.last().x, points.first().y + 10f),
                                strokeWidth = ann.strokeWidth
                            )
                        } else {
                            drawPath(
                                path = path,
                                color = color,
                                style = Stroke(
                                    width = ann.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
                
                // Draw current path
                if (currentPath.isNotEmpty() && currentTool != AnnotationTool.ERASER) {
                    val path = Path().apply {
                        moveTo(currentPath.first().x, currentPath.first().y)
                        for (i in 1 until currentPath.size) {
                            lineTo(currentPath[i].x, currentPath[i].y)
                        }
                    }
                    val alpha = if (currentTool == AnnotationTool.HIGHLIGHTER) 0.4f else 1f
                    val sWidth = if (currentTool == AnnotationTool.HIGHLIGHTER) 20f else strokeWidth
                    
                    if (currentTool == AnnotationTool.UNDERLINE) {
                        drawLine(
                            color = selectedColor,
                            start = Offset(currentPath.first().x, currentPath.first().y + 10f),
                            end = Offset(currentPath.last().x, currentPath.first().y + 10f),
                            strokeWidth = sWidth
                        )
                    } else {
                        drawPath(
                            path = path,
                            color = selectedColor.copy(alpha = alpha),
                            style = Stroke(
                                width = sWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        } else {
            // Placeholder for loading page
            Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color.LightGray)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
