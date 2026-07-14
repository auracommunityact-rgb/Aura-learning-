import re

with open("app/src/main/java/com/example/ui/books/PdfViewerScreen.kt", "r") as f:
    content = f.read()

# Make sure we have the imports we need
if "import androidx.compose.material.icons.filled.KeyboardArrowLeft" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.KeyboardArrowLeft\nimport androidx.compose.material.icons.filled.KeyboardArrowRight")

if "import androidx.compose.foundation.text.KeyboardOptions" not in content:
    content = content.replace("import androidx.compose.foundation.text.KeyboardOptions", "")
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.text.KeyboardOptions\nimport androidx.compose.ui.text.input.KeyboardType")

# We need to find the bottom toolbar
bottom_toolbar_regex = r"(\s*)// Bottom Floating Toolbar\s*AnimatedVisibility\(\s*visible = isUIVisible \|\| currentTool != AnnotationTool\.NONE,\s*enter = slideInVertically.*?exit = slideOutVertically.*?modifier = Modifier\s*\.align\(Alignment\.BottomCenter\)\s*\.padding\(bottom = 24\.dp\)\s*\.windowInsetsPadding\(WindowInsets\.navigationBars\)\s*\)\s*\{"

replacement = r"""\1// Bottom Controls Container
\1Column(
\1    modifier = Modifier
\1        .align(Alignment.BottomCenter)
\1        .windowInsetsPadding(WindowInsets.navigationBars)
\1        .padding(bottom = 24.dp),
\1    horizontalAlignment = Alignment.CenterHorizontally,
\1    verticalArrangement = Arrangement.spacedBy(16.dp)
\1) {
\1    // Page Navigation
\1    var showPageJumpDialog by remember { mutableStateOf(false) }
\1    
\1    AnimatedVisibility(
\1        visible = isUIVisible && currentTool == AnnotationTool.NONE,
\1        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
\1        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
\1    ) {
\1        Card(
\1            modifier = Modifier
\1                .padding(horizontal = 16.dp)
\1                .fillMaxWidth(0.9f)
\1                .shadow(8.dp, RoundedCornerShape(24.dp)),
\1            shape = RoundedCornerShape(24.dp),
\1            colors = CardDefaults.cardColors(containerColor = topBarColor)
\1        ) {
\1            Column(
\1                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
\1            ) {
\1                Row(
\1                    modifier = Modifier.fillMaxWidth(),
\1                    horizontalArrangement = Arrangement.SpaceBetween,
\1                    verticalAlignment = Alignment.CenterVertically
\1                ) {
\1                    IconButton(
\1                        onClick = { 
\1                            coroutineScope.launch {
\1                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
\1                            }
\1                        },
\1                        enabled = pagerState.currentPage > 0
\1                    ) {
\1                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous Page", tint = if (pagerState.currentPage > 0) contentColor else contentColor.copy(alpha = 0.3f))
\1                    }
\1                    
\1                    Text(
\1                        text = "Page ${pagerState.currentPage + 1} of $pageCount",
\1                        style = MaterialTheme.typography.labelLarge,
\1                        color = contentColor,
\1                        modifier = Modifier
\1                            .clip(RoundedCornerShape(8.dp))
\1                            .clickable { showPageJumpDialog = true }
\1                            .padding(horizontal = 16.dp, vertical = 8.dp)
\1                    )
\1                    
\1                    IconButton(
\1                        onClick = {
\1                            coroutineScope.launch {
\1                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
\1                            }
\1                        },
\1                        enabled = pagerState.currentPage < pageCount - 1
\1                    ) {
\1                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next Page", tint = if (pagerState.currentPage < pageCount - 1) contentColor else contentColor.copy(alpha = 0.3f))
\1                    }
\1                }
\1                
\1                Slider(
\1                    value = pagerState.currentPage.toFloat(),
\1                    onValueChange = { 
\1                        coroutineScope.launch { 
\1                            pagerState.scrollToPage(it.toInt()) 
\1                        } 
\1                    },
\1                    valueRange = 0f..maxOf(0f, (pageCount - 1).toFloat()),
\1                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
\1                )
\1            }
\1        }
\1    }
\1    
\1    if (showPageJumpDialog) {
\1        var jumpPageText by remember { mutableStateOf((pagerState.currentPage + 1).toString()) }
\1        AlertDialog(
\1            onDismissRequest = { showPageJumpDialog = false },
\1            title = { Text("Jump to Page") },
\1            text = {
\1                OutlinedTextField(
\1                    value = jumpPageText,
\1                    onValueChange = { jumpPageText = it.filter { char -> char.isDigit() } },
\1                    singleLine = true,
\1                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
\1                    label = { Text("Page Number") }
\1                )
\1            },
\1            confirmButton = {
\1                TextButton(onClick = {
\1                    val page = jumpPageText.toIntOrNull()
\1                    if (page != null && page in 1..pageCount) {
\1                        coroutineScope.launch {
\1                            pagerState.animateScrollToPage(page - 1)
\1                        }
\1                    }
\1                    showPageJumpDialog = false
\1                }) {
\1                    Text("Go")
\1                }
\1            },
\1            dismissButton = {
\1                TextButton(onClick = { showPageJumpDialog = false }) {
\1                    Text("Cancel")
\1                }
\1            }
\1        )
\1    }
\1
\1    // Bottom Floating Toolbar
\1    AnimatedVisibility(
\1        visible = isUIVisible || currentTool != AnnotationTool.NONE,
\1        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
\1        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
\1    ) {"""

content = re.sub(bottom_toolbar_regex, replacement, content, flags=re.DOTALL)

# Find the end of the AnimatedVisibility for Bottom Floating Toolbar and add a closing bracket for the Column
# The animated visibility ends around line 567. We can just find `} // End of PdfViewerScreen Box` or something.
# Let's use a simpler approach.

with open("app/src/main/java/com/example/ui/books/PdfViewerScreen.kt", "w") as f:
    f.write(content)

