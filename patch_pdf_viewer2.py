import re

with open("app/src/main/java/com/example/ui/books/PdfViewerScreen.kt", "r") as f:
    content = f.read()

# Find pagerState = rememberPagerState...
target_str = r"val pagerState = rememberPagerState\(pageCount = \{ pageCount \}\)"

replacement = """val initialPage by viewModel.initialPage.collectAsState()
    val pagerState = rememberPagerState(pageCount = { pageCount })
    
    LaunchedEffect(initialPage, pageCount) {
        if (pageCount > 0 && initialPage > 0 && initialPage < pageCount) {
            pagerState.scrollToPage(initialPage)
        }
    }
    
    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateProgress(bookId, pagerState.currentPage)
    }"""

content = re.sub(target_str, replacement, content)

with open("app/src/main/java/com/example/ui/books/PdfViewerScreen.kt", "w") as f:
    f.write(content)

