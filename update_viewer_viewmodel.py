import re

with open("app/src/main/java/com/example/ui/books/PdfViewerViewModel.kt", "r") as f:
    content = f.read()

# Add initialPage state
initial_page_state = """    private val _initialPage = MutableStateFlow(0)
    val initialPage: StateFlow<Int> = _initialPage"""

content = content.replace("private val _pdfPageCount", initial_page_state + "\n\n    private val _pdfPageCount")

# Inside loadPdf, before processing file:
load_progress = """                val userId = SupabaseService.client.auth.currentSessionOrNull()?.user?.id
                if (userId != null) {
                    val progressList = auraRepo.getBookProgress(userId)
                    val progress = progressList.find { it.bookId == bookId }
                    if (progress != null) {
                        _initialPage.value = progress.lastPage
                    }
                }
"""

content = content.replace("val file: File", load_progress + "\n                val file: File")

with open("app/src/main/java/com/example/ui/books/PdfViewerViewModel.kt", "w") as f:
    f.write(content)

